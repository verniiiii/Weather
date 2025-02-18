package com.example.myweather.decorator

import android.util.Log

interface TemperatureCalculator {
    fun calculateAverageTemperature(temperatures: List<Double>): Double
}

class BasicTemperatureCalculator : TemperatureCalculator {
    override fun calculateAverageTemperature(temperatures: List<Double>): Double {
        return temperatures.average()
    }
}

abstract class TemperatureCalculatorDecorator(
    private val calculator: TemperatureCalculator
) : TemperatureCalculator by calculator

class TemperatureLoggerDecorator(
    calculator: TemperatureCalculator
) : TemperatureCalculatorDecorator(calculator) {

    override fun calculateAverageTemperature(temperatures: List<Double>): Double {
        val averageTemperature = super.calculateAverageTemperature(temperatures)
        Log.d("TemperatureLogger", "Средняя температура: $averageTemperature")
        return averageTemperature
    }

    fun logTemperature(cityName: String, season: String, averageTemperature: Double) {
        Log.d("TemperatureLogger", "Город: $cityName, Сезон: $season, Средняя температура: $averageTemperature")
    }
}
