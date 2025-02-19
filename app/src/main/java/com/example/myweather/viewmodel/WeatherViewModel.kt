package com.example.myweather.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.myweather.data.City
import com.example.myweather.data.dao.CityDao
import com.example.myweather.constants.Month
import com.example.myweather.constants.Season
import com.example.myweather.data.Temperature
import com.example.myweather.data.dao.TemperatureDao
import com.example.myweather.decorator.BasicTemperatureCalculator
import com.example.myweather.decorator.TemperatureLoggerDecorator
import com.example.myweather.factory.CityTypeFactory
import com.example.myweather.factory.DefaultCityTypeFactory
import com.example.myweather.strategy.CelsiusFormatStrategy
import com.example.myweather.strategy.FahrenheitFormatStrategy
import com.example.myweather.strategy.KelvinFormatStrategy
import com.example.myweather.strategy.TemperatureFormatStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel(private val context: Context,
                       private val cityDao: CityDao,
                       private val temperatureDao: TemperatureDao
    //temperatureDao = db.temperatureDao()
    ) : ViewModel() {

    private val cityTypeFactory: CityTypeFactory = DefaultCityTypeFactory()
    private val temperatureCalculator = TemperatureLoggerDecorator(BasicTemperatureCalculator())


    private val _selectedCity = MutableLiveData<City>()
    private val _selectedSeason = MutableLiveData<String>()

    val cityType: LiveData<String> = MutableLiveData<String>()
    private val _averageTemperature = MutableLiveData<Double>()
    val averageTemperature: LiveData<String> = MutableLiveData<String>()
    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> get() = _cities
    private val _seasons = MutableLiveData<List<String>>()
    val seasons: LiveData<List<String>> get() = _seasons
    val selectedSeason: LiveData<String> = _selectedSeason
    val selectedCity: LiveData<City> = _selectedCity

    private val _temperatureFormat = MutableLiveData<TemperatureFormatStrategy>()
    val temperatureFormat: LiveData<TemperatureFormatStrategy> get() = _temperatureFormat

    private val _selectedTemperatureFormat = MutableLiveData<String>()
    val selectedTemperatureFormat: LiveData<String> get() = _selectedTemperatureFormat

    init {
        _selectedTemperatureFormat.value = "Цельсий" // Установите начальный формат
        _temperatureFormat.value = CelsiusFormatStrategy() // Установите начальный формат
        //val db = WeatherDatabase.getDatabase(application)
        //cityDao = db.cityDao()
        //temperatureDao = db.temperatureDao()
        loadCities()
    }



    private fun loadCities() {
        viewModelScope.launch {
            val cities = cityDao.getAllCities().map { city ->
                val type = cityTypeFactory.getCityType(city.name)
                city.copy(type = type ?: city.type) // Если тип null, оставляем текущий тип
            }
            _cities.value = cities
        }
    }

    fun selectCity(cityName: String) {
        viewModelScope.launch {
            val city = cityDao.getCityByName(cityName)
            if (city != null) {
                _selectedCity.value = city!!
                updateCityInfo(city)
                loadSeasonsForCity(city.id) // Загрузка сезонов для выбранного города
                resetSeasonSelection() // Сброс выбора сезона
            } else {
                Toast.makeText(context, "Город не найден", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun selectSeason(seasonName: String) {
        _selectedSeason.value = seasonName
        updateTemperatureInfo(_selectedCity.value, seasonName)
    }

    fun setTemperatureFormat(format: TemperatureFormatStrategy) {
        _temperatureFormat.value = format
        _selectedTemperatureFormat.value = when (format) {
            is CelsiusFormatStrategy -> "Цельсий"
            is FahrenheitFormatStrategy -> "Фаренгейт"
            is KelvinFormatStrategy -> "Кельвин"
            else -> "Цельсий"
        }
        updateTemperatureDisplay()
    }

    private fun updateCityInfo(city: City) {
        (cityType as MutableLiveData<String>).value = city.type
    }

    private fun updateTemperatureInfo(city: City?, seasonName: String) {
        city?.let {
            viewModelScope.launch {
                val temperatures = temperatureDao.getTemperaturesByCityAndSeason(it.id, seasonName)
                val averageTemp = temperatureCalculator.calculateAverageTemperature(temperatures.map { temp -> temp.temperature })
                _averageTemperature.value = averageTemp
                updateTemperatureDisplay()
                temperatureCalculator.logTemperature(city.name, seasonName, averageTemp)
            }
        }
    }

    private fun updateTemperatureDisplay() {
        val averageTemp = _averageTemperature.value ?: 0.0
        val currentFormat = _temperatureFormat.value ?: CelsiusFormatStrategy() // Используйте текущий формат или установите значение по умолчанию
        (averageTemperature as MutableLiveData<String>).value = currentFormat.formatTemperature(averageTemp)
    }



    private fun loadSeasonsForCity(cityId: Int) {
        viewModelScope.launch {
            val city = cityDao.getCityById(cityId)
            if (city != null) {
                val temperatures = temperatureDao.getTemperaturesByCity(cityId)
                val seasonsList = temperatures.map { it.season }.distinct()
                _seasons.value = seasonsList
            } else {
                Log.e("WeatherViewModel", "Город с id $cityId не найден.")
            }
        }
    }


    private fun resetSeasonSelection() {
        _selectedSeason.value = ""
        (averageTemperature as MutableLiveData<String>).value = "Средняя температура"
    }

    fun addCity(cityName: String, cityType: String) {
        if (cityName.isBlank()) {
            Toast.makeText(context, "Имя города не может быть пустым", Toast.LENGTH_SHORT).show()
            return
        }

        if (cityType.isBlank()) {
            Toast.makeText(context, "Тип города не может быть пустым", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val city = City(name = cityName, type = cityType)
            cityDao.insertCity(city)
            loadCities()
        }
    }

    fun addTemperature(cityName: String, month: String, temperature: Double) {
        if (cityName.isBlank()) {
            Toast.makeText(context, "Имя города не может быть пустым", Toast.LENGTH_SHORT).show()
            return
        }

        if (temperature !in -50.0..50.0) {
            Toast.makeText(context, "Температура должна быть в диапазоне от -50 до 50", Toast.LENGTH_SHORT).show()
            return
        }

        if (month !in listOf(Month.ЯНВАРЬ, Month.ФЕВРАЛЬ, Month.МАРТ, Month.АПРЕЛЬ, Month.МАЙ, Month.ИЮНЬ, Month.ИЮЛЬ, Month.АВГУСТ, Month.СЕНТЯБРЬ, Month.ОКТЯБРЬ, Month.НОЯБРЬ, Month.ДЕКАБРЬ)) {
            Toast.makeText(context, "Недопустимый месяц", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val city = cityDao.getCityByName(cityName)
            if (city != null) {
                val season = when (month) {
                    Month.ДЕКАБРЬ, Month.ЯНВАРЬ, Month.ФЕВРАЛЬ -> Season.ЗИМА
                    Month.МАРТ, Month.АПРЕЛЬ, Month.МАЙ -> Season.ВЕСНА
                    Month.ИЮНЬ, Month.ИЮЛЬ, Month.АВГУСТ -> Season.ЛЕТО
                    Month.СЕНТЯБРЬ, Month.ОКТЯБРЬ, Month.НОЯБРЬ -> Season.ОСЕНЬ
                    else -> ""
                }

                // Проверяем, существует ли уже запись с таким городом и месяцем
                val existingTemperature = temperatureDao.getTemperature(city.id, month)

                if (existingTemperature != null) {
                    // Если температура уже существует, обновляем её
                    val updatedTemp = existingTemperature.copy(temperature = temperature, season = season)
                    temperatureDao.updateTemperature(updatedTemp)
                    Log.d("TemperatureLogger", "Температура для города ${city.name} в месяце $month обновлена на $temperature")
                } else {
                    // Если температуры нет, добавляем новую
                    val temp = Temperature(cityId = city.id, month = month, temperature = temperature, season = season)
                    temperatureDao.insertTemperature(temp)
                    Log.d("TemperatureLogger", "Температура для города ${city.name} в месяце $month добавлена: $temperature")
                }

                updateTemperatureInfo(city, month)
            }
        }
    }




    fun deleteCity(city: City) {
        viewModelScope.launch {
            cityDao.deleteCity(city)
            loadCities()
        }
    }





    suspend fun getAverageTemperatureBySeason(cityId: Int, season: String): Double {
        return withContext(Dispatchers.IO) {
            // Получаем список температур для указанного города и сезона
            val temperatures = temperatureDao.getTemperaturesByCityAndSeason(cityId, season)

            // Логируем каждую температуру для проверки
            temperatures.forEach { temp ->
                Log.d("TemperatureLogger", "Месяц: ${temp.month}, Температура: ${temp.temperature}")
            }

            // Рассчитываем среднюю температуру
            val averageTemp = temperatures.map { it.temperature }.average()

            // Логируем результат средней температуры
            Log.d("TemperatureLogger", "Средняя температура для города с id $cityId и сезона $season: $averageTemp")

            return@withContext averageTemp
        }
    }

    // Метод для предварительного заполнения базы данных
    fun prefillDatabase() {
        viewModelScope.launch {
            if (cityDao.getAllCities().isEmpty()) {
                val cities = listOf(
                    City(name = "Москва", type = "Большой"),
                    City(name = "Санкт-Петербург", type = "Большой"),
                    City(name = "Новосибирск", type = "Средний")
                )

                cities.forEach { city ->
                    cityDao.insertCity(city)
                }

                // Получаем обновленный список городов с их идентификаторами
                val updatedCities = cityDao.getAllCities()

                updatedCities.forEach { city ->
                    val temperatures = listOf(
                        Temperature(cityId = city.id, month = "Январь", temperature = -10.0 + Math.random() * 5, season = "Зима"),
                        Temperature(cityId = city.id, month = "Февраль", temperature = -5.0 + Math.random() * 5, season = "Зима"),
                        Temperature(cityId = city.id, month = "Март", temperature = 0.0 + Math.random() * 5, season = "Весна"),
                        Temperature(cityId = city.id, month = "Апрель", temperature = 10.0 + Math.random() * 5, season = "Весна"),
                        Temperature(cityId = city.id, month = "Май", temperature = 15.0 + Math.random() * 5, season = "Весна"),
                        Temperature(cityId = city.id, month = "Июнь", temperature = 20.0 + Math.random() * 5, season = "Лето"),
                        Temperature(cityId = city.id, month = "Июль", temperature = 25.0 + Math.random() * 5, season = "Лето"),
                        Temperature(cityId = city.id, month = "Август", temperature = 22.0 + Math.random() * 5, season = "Лето"),
                        Temperature(cityId = city.id, month = "Сентябрь", temperature = 17.0 + Math.random() * 5, season = "Осень"),
                        Temperature(cityId = city.id, month = "Октябрь", temperature = 10.0 + Math.random() * 5, season = "Осень"),
                        Temperature(cityId = city.id, month = "Ноябрь", temperature = 5.0 + Math.random() * 5, season = "Осень"),
                        Temperature(cityId = city.id, month = "Декабрь", temperature = -5.0 + Math.random() * 5, season = "Зима")
                    )
                    temperatures.forEach { temperature ->
                        temperatureDao.insertTemperature(temperature)
                    }
                }

                loadCities()
            }
        }
    }
}

