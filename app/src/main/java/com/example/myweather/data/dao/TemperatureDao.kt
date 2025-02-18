package com.example.myweather.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myweather.data.Temperature

@Dao
interface TemperatureDao {
    @Insert
    suspend fun insertTemperature(temperature: Temperature)

    @Update
    suspend fun updateTemperature(temperature: Temperature)

    @Delete
    suspend fun deleteTemperature(temperature: Temperature)

    @Query("SELECT * FROM temperatures WHERE cityId = :cityId AND season = :season")
    suspend fun getTemperaturesByCityAndSeason(cityId: Int, season: String): List<Temperature>

    @Query("SELECT * FROM temperatures WHERE cityId = :cityId")
    suspend fun getTemperaturesByCity(cityId: Int): List<Temperature>
}
