package com.example.apkmoblineprojekt

import android.app.Application

class BliskoApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
