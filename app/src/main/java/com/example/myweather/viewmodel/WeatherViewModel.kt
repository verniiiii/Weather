package com.example.myweather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WeatherViewModel: ViewModel() {
    private val _selectedCity = MutableLiveData<String>()
    private val _selectedSeason = MutableLiveData<String>()

    val cityType: LiveData<String> = MutableLiveData()
    val averageTemperature: LiveData<String> = MutableLiveData()

    fun selectCity(cityName: String){
        _selectedCity.value = cityName
        updateCityInfo(cityName)
    }

    fun selectSeason(seasonName: String){
        _selectedSeason.value = seasonName
        updateTemperatureInfo(_selectedCity.value, seasonName)
    }

    fun saveCitySettings(cityName: String, cityType: String, juneTemp: Double?, julyTemp: Double?, augustTemp: Double?){
        //Сохранить настройки города в базе данных или другом хранилище
        //Обновить LiveData для отображения новых данных
        (this.cityType as MutableLiveData).value = cityType
        val averageTemp = ((juneTemp ?: 0.0) + (julyTemp ?: 0.0) + (augustTemp ?: 0.0)) / 3
        (averageTemperature as MutableLiveData).value = "$averageTemp°C"
    }

    private fun updateCityInfo(cityName: String){
        val type = when(cityName){
            "Минск" -> "Средний"
            "Москва" -> "Большой"
            "Казань" -> "Средний"
            else -> "Малый"
        }
        (cityType as MutableLiveData).value = type
    }

    private fun updateTemperatureInfo(cityName: String?, seasonName: String){
        val averageTemp = when(seasonName){
            "Лето" -> 25.3
            else -> 0.0
        }
        (averageTemperature as MutableLiveData).value = "$averageTemp°C"
    }
}