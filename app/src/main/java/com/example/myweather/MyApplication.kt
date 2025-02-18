package com.example.myweather

import android.app.Application
import com.example.myweather.di.RoomModule
import com.example.myweather.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@MyApplication)
            modules(listOf(RoomModule, appModule))
        }
    }
}