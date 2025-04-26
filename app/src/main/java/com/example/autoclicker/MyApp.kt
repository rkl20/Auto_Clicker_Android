package com.example.autoclicker

import android.app.Application
//store global var here
class MyApp: Application() {
    var previousActivity: String? = null
    override fun onCreate() {
        super.onCreate()
    }
}