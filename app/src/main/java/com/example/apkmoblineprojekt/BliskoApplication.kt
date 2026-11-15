package com.example.apkmoblineprojekt

import android.app.Application
import com.example.apkmoblineprojekt.reminders.Notifications

class BliskoApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannels(this)
    }
}
