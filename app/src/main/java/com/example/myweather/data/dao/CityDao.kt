package com.example.myweather.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myweather.data.City


@Dao
interface CityDao {
    @Insert
    suspend fun insertCity(city: City)

    @Update
    suspend fun updateCity(city: City)

    @Delete
    suspend fun deleteCity(city: City)

    @Query("SELECT * FROM cities")
    suspend fun getAllCities(): List<City>

    @Query("SELECT * FROM cities WHERE name = :cityName")
    suspend fun getCityByName(cityName: String): City?

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCityById(cityId: Int): City?
}
