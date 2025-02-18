package com.example.myweather.factory

class DefaultCityTypeFactory: CityTypeFactory {
    override fun getCityType(cityName: String): String? {
        return when(cityName.lowercase()){
            "москва", "санкт-петербург" -> "Большой"
            "новосибирск", "екатеринбург" -> "Средний"
            else -> null
        }
    }
}