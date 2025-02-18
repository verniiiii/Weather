package com.example.myweather.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Создаем интерсептор для логирования
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Создаем клиент OkHttp с логированием
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Настраиваем Retrofit с использованием клиента OkHttp
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://geocode-maps.yandex.ru/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    // Создаем экземпляр API
    val yandexGeocoderApi: YandexGeocoderApi = retrofit.create(YandexGeocoderApi::class.java)
}
