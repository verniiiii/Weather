package com.example.myweather.network


import YandexGeocoderResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YandexGeocoderApi {
    @GET("1.x")
    suspend fun getCityInfo(
        @Query("apikey") apiKey: String,
        @Query("geocode") geocode: String,
        @Query("format") format: String = "json",
        @Query("lang") lang: String = "ru_RU"
    ): YandexGeocoderResponse
}
