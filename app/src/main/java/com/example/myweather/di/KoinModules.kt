package com.example.myweather.di

import com.example.myweather.data.WeatherDatabase
import com.example.myweather.viewmodel.WeatherViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val RoomModule = module {
    single { WeatherDatabase.getDatabase(get()) }
    single { get<WeatherDatabase>().cityDao() }
    single { get<WeatherDatabase>().temperatureDao() }
}

val appModule = module {
    single { androidApplication() }
    viewModel { WeatherViewModel(get(), get(), get()) }
}
