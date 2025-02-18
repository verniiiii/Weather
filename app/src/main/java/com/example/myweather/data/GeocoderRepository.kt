package com.example.myweather.data

import FeatureMember
import android.util.Log
import com.example.myweather.network.RetrofitClient
import com.example.myweather.network.YandexGeocoderApi

class GeocoderRepository {
    private val api = RetrofitClient.yandexGeocoderApi

    suspend fun fetchCityInfo(latitude: Double, longitude: Double): String {
        val apiKey = "9a8a6d9f-9501-4d19-84ec-fea83fb11065" // Замените на ваш реальный API ключ
        val response = api.getCityInfo(
            apiKey = apiKey,
            geocode = "$longitude,$latitude"
        )

        // Логирование всего ответа
        Log.d("GeocoderResponse", "Full Response: $response")

        // Логирование GeoObjectCollection
        val geoObjectCollection = response.response.geoObjectCollection
        Log.d("GeocoderResponse", "GeoObjectCollection: $geoObjectCollection")

        // Проверка на null перед вызовом firstOrNull
        val geoObjects: List<FeatureMember> = geoObjectCollection.featureMember
        if (geoObjects.isNullOrEmpty()) {
            Log.d("GeocoderResponse", "No geo objects found")
            return "Unknown City"
        }

        // Логирование каждого GeoObjectFeature
        geoObjects.forEach { geoObjectFeature ->
            Log.d("GeocoderResponse", "GeoObjectFeature: $geoObjectFeature")

            // Логирование GeoObject
            val geoObject = geoObjectFeature.geoObject
            Log.d("GeocoderResponse", "GeoObject: $geoObject")

            // Логирование GeocoderMetaData
            val metaDataProperty = geoObject.metaDataProperty
            Log.d("GeocoderResponse", "GeocoderMetaData: $metaDataProperty")

            // Логирование текста и типа объекта
            val text = metaDataProperty.geocoderMetaData.text
            val kind = metaDataProperty.geocoderMetaData.kind
            Log.d("GeocoderResponse", "Text: $text, Kind: $kind")
        }

        // Поиск объекта с типом "locality"
        val cityObject = geoObjects.firstOrNull { geoObjectFeature ->
            geoObjectFeature.geoObject.metaDataProperty.geocoderMetaData.kind == "locality"
        }

        if (cityObject != null) {
            val cityName = cityObject.geoObject.metaDataProperty.geocoderMetaData.text
            Log.d("GeocoderResponse", "City Object Found: $cityName")
            return cityName
        } else {
            Log.d("GeocoderResponse", "No city object found with kind 'locality'")
            return "Unknown City"
        }
    }
}
