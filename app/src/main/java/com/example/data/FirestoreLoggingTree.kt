package com.example.data

import android.content.Context
import android.os.Build
import android.util.Log
import com.aistudio.provalino.teacher.abcxyz.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Custom Timber Tree that captures logs and forwards WARN / ERROR / CRITICAL events
 * directly to the 'log_erro' collection in Firebase Firestore.
 */
class FirestoreLoggingTree(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : Timber.Tree() {

    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // In logcat, we let default behavior or explicit print happen
        if (priority < Log.WARN) {
            return
        }

        // Only log WARN and ERROR to Firestore to save quota and bandwidth
        scope.launch {
            try {
                recordErrorToFirestore(
                    priority = priority,
                    tag = tag ?: "ProvalinoApp",
                    message = message,
                    throwable = t
                )
            } catch (e: Throwable) {
                // Fail silently to avoid crash loops in logger
            }
        }
    }

    private fun recordErrorToFirestore(
        priority: Int,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val db = firestore ?: return
        val currentTime = System.currentTimeMillis()
        val formattedDate = isoDateFormat.format(Date(currentTime))

        val levelName = when (priority) {
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "INFO"
        }

        val stackTraceStr = throwable?.stackTraceToString() ?: if (priority >= Log.ERROR) {
            Thread.currentThread().stackTrace.take(15).joinToString("\n") { it.toString() }
        } else {
            ""
        }

        val prefs = context.getSharedPreferences("provalino_prefs", Context.MODE_PRIVATE)
        val cachedUserId = prefs.getString("cached_teacher_id", "anonymous_user") ?: "anonymous_user"

        val errorPayload = hashMapOf<String, Any>(
            "timestamp" to currentTime,
            "formatted_date" to formattedDate,
            "app_version_name" to BuildConfig.VERSION_NAME,
            "app_version_code" to BuildConfig.VERSION_CODE,
            "level" to levelName,
            "tag" to tag,
            "category" to if (throwable != null) "EXCEPTION" else "LOG_EVENT",
            "message" to message,
            "exception_class" to (throwable?.javaClass?.name ?: ""),
            "stack_trace" to stackTraceStr,
            "device_model" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "android_version" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "thread_name" to Thread.currentThread().name,
            "user_id" to cachedUserId,
            "environment" to if (BuildConfig.DEBUG) "development" else "production"
        )

        db.collection("log_erro")
            .add(errorPayload)
            .addOnFailureListener {
                // Ignore failure to prevent recursive logging
            }
    }
}
