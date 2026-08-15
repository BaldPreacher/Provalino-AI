package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

class ProvalinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            com.example.data.AnalyticsRepository.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
