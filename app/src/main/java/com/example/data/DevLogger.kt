package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DevErrorLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val category: String,
    val message: String,
    val stackTrace: String = ""
)

object DevLogger {
    private const val TAG = "DevLogger"
    private const val PREFS_NAME = "provalino_dev_logs"
    private const val KEY_LOGS = "error_logs_json"

    private val _logs = MutableStateFlow<List<DevErrorLog>>(emptyList())
    val logs: StateFlow<List<DevErrorLog>> = _logs.asStateFlow()

    fun initialize(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_LOGS, "") ?: ""
            if (raw.isNotBlank()) {
                val parsed = raw.split("|||").mapNotNull { entry ->
                    val parts = entry.split(":::")
                    if (parts.size >= 4) {
                        DevErrorLog(
                            id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                            timestamp = parts[1],
                            category = parts[2],
                            message = parts[3],
                            stackTrace = if (parts.size > 4) parts[4] else ""
                        )
                    } else null
                }
                _logs.value = parsed.take(50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar DevLogger: ${e.message}")
        }
    }

    fun logError(context: Context?, category: String, message: String, throwable: Throwable? = null) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val stackStr = throwable?.stackTraceToString() ?: ""

        val newLog = DevErrorLog(
            timestamp = timeStr,
            category = category,
            message = message,
            stackTrace = stackStr
        )

        val updated = (listOf(newLog) + _logs.value).take(50)
        _logs.value = updated

        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val rawString = updated.joinToString("|||") {
                    "${it.id}:::${it.timestamp}:::${it.category}:::${it.message}:::${it.stackTrace}"
                }
                prefs.edit().putString(KEY_LOGS, rawString).apply()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar log no DevLogger: ${e.message}")
            }
        }
        Log.e(TAG, "[$category] $message ${if (stackStr.isNotBlank()) "\n$stackStr" else ""}")
    }

    fun clearLogs(context: Context) {
        _logs.value = emptyList()
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_LOGS).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar logs: ${e.message}")
        }
    }
}
