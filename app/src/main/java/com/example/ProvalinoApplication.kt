package com.example

import android.app.Application
import android.util.Log
import com.aistudio.provalino.teacher.abcxyz.BuildConfig
import com.example.data.FirestoreLoggingTree
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import timber.log.Timber

class ProvalinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Plant Timber Trees for structured logging across the entire app
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(FirestoreLoggingTree(this))
        Timber.tag("ProvalinoLifecycle").i("ProvalinoApplication.onCreate started with Timber")

        // Uncaught Exception Handler to capture crash traces immediately
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.tag("ProvalinoCrash").e(throwable, "FATAL CRASH on thread %s: %s", thread.name, throwable.message)
            try {
                com.example.data.DevLogger.logError(
                    context = applicationContext,
                    category = "FATAL_APP_CRASH",
                    message = "Crash na thread ${thread.name}: ${throwable.message}",
                    throwable = throwable
                )
            } catch (e: Exception) {
                // Secondary error suppression
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Initialize FirebaseApp safely before any ViewModel or Firestore access
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
                Log.i("ProvalinoLifecycle", "FirebaseApp initialized with options successfully")
            } else {
                Log.i("ProvalinoLifecycle", "FirebaseApp already initialized")
            }
        } catch (e: Exception) {
            Log.e("ProvalinoLifecycle", "Error initializing FirebaseApp: ${e.message}", e)
        }

        // Initialize Analytics Repository
        try {
            com.example.data.AnalyticsRepository.initialize(this)
            Log.i("ProvalinoLifecycle", "AnalyticsRepository initialized")
        } catch (e: Exception) {
            Log.e("ProvalinoLifecycle", "Error initializing AnalyticsRepository: ${e.message}", e)
        }

        // Initialize DevLogger
        try {
            com.example.data.DevLogger.initialize(this)
            Log.i("ProvalinoLifecycle", "DevLogger initialized")
        } catch (e: Exception) {
            Log.e("ProvalinoLifecycle", "Error initializing DevLogger: ${e.message}", e)
        }

        // Initialize AdMob
        try {
            com.example.ads.AdMobManager.initialize(this)
            Log.i("ProvalinoLifecycle", "AdMobManager initialized")
        } catch (e: Exception) {
            Log.e("ProvalinoLifecycle", "Error initializing AdMobManager: ${e.message}", e)
        }

        Log.i("ProvalinoLifecycle", "ProvalinoApplication.onCreate finished")
    }
}

