package com.example.myweather.strategy

interface TemperatureFormatStrategy {
    fun formatTemperature(temperature: Double): String
}


class CelsiusFormatStrategy : TemperatureFormatStrategy {
    override fun formatTemperature(temperature: Double): String {
        return "${String.format("%.1f", temperature)}°C"
    }
}

class FahrenheitFormatStrategy : TemperatureFormatStrategy {
    override fun formatTemperature(temperature: Double): String {
        val fahrenheit = (temperature * 9 / 5) + 32
        return "${String.format("%.1f", fahrenheit)}°F"
    }
}

class KelvinFormatStrategy : TemperatureFormatStrategy {
    override fun formatTemperature(temperature: Double): String {
        val kelvin = temperature + 273.15
        return "${String.format("%.1f", kelvin)}K"
    }
}
