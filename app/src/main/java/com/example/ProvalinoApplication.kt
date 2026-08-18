package com.example

import android.app.Application
import com.aistudio.provalino.teacher.abcxyz.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ProvalinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val apiKey = if (BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                    BuildConfig.GEMINI_API_KEY
                } else {
                    "AIzaSyB1CT13IqEQL2Z7f6GaY3vfAeyl02PCWQs"
                }

                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId("1:12454269674:android:05302ac67950fefe0afd93")
                    .setProjectId("provalino-ia-provas-adaptadas")
                    .setStorageBucket("provalino-ia-provas-adaptadas.firebasestorage.app")
                    .setGcmSenderId("12454269674")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
            com.example.data.AnalyticsRepository.initialize(this)
            com.example.data.DevLogger.initialize(this)
            com.example.ads.AdMobManager.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
