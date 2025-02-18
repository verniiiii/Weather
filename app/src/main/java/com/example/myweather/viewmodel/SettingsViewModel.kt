package com.example.myweather.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myweather.data.City
import com.example.myweather.data.CityDao
import com.example.myweather.data.Temperature
import com.example.myweather.data.TemperatureDao
import com.example.myweather.data.WeatherDatabase
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val cityDao: CityDao
    private val temperatureDao: TemperatureDao
    private val context: Context = application.applicationContext

    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> get() = _cities

    init {
        val db = WeatherDatabase.getDatabase(application)
        cityDao = db.cityDao()
        temperatureDao = db.temperatureDao()
        loadCities()
    }

    private fun loadCities() {
        viewModelScope.launch {
            val cities = cityDao.getAllCities()
            Log.d("SettingsViewModel", "Loaded cities: $cities")
            _cities.value = cities
        }
    }

    fun addCity(city: City, onSuccess: (City) -> Unit) {
        viewModelScope.launch {
            cityDao.insertCity(city)
            loadCities()
            onSuccess(city)
        }
    }

    fun updateCity(city: City) {
        viewModelScope.launch {
            cityDao.updateCity(city)
            loadCities()
        }
    }

    fun deleteCity(cityName: String) {
        viewModelScope.launch {
            val city = cityDao.getCityByName(cityName)
            if (city != null) {
                cityDao.deleteCity(city)
                loadCities()
            } else {
                Toast.makeText(context, "Город не найден", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addTemperature(temperature: Temperature) {
        viewModelScope.launch {
            temperatureDao.insertTemperature(temperature)
        }
    }

    fun updateTemperature(temperature: Temperature) {
        viewModelScope.launch {
            temperatureDao.updateTemperature(temperature)
        }
    }

    fun deleteTemperature(temperature: Temperature) {
        viewModelScope.launch {
            temperatureDao.deleteTemperature(temperature)
        }
    }

    // Метод для добавления города с проверкой на пустые поля
    fun addCityWithValidation(cityName: String, cityType: String, onSuccess: (City) -> Unit) {
        if (cityName.isBlank() || cityType.isBlank()) {
            Toast.makeText(context, "Поля для названия города и типа города не могут быть пустыми", Toast.LENGTH_SHORT).show()
        } else {
            val city = City(name = cityName, type = cityType)
            addCity(city, onSuccess)
        }
    }


}

