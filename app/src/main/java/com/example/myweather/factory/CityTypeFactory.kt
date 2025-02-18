package com.example.myweather.factory

interface CityTypeFactory{
    fun getCityType(cityName: String): String?
}