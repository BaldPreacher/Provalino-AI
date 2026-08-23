package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class AppUpdateState(
    val isUpdateAvailable: Boolean = false,
    val isForceUpdate: Boolean = false,
    val installedVersionCode: Long = 0,
    val latestVersionCode: Long = 0,
    val latestVersionName: String = "",
    val updateTitle: String = "Nova Versão Disponível! 🚀",
    val updateMessage: String = "Uma nova versão com melhorias e correções está disponível na Google Play Store. Atualize agora para continuar aproveitando!",
    val releaseNotes: String = "",
    val updateUrl: String = "https://play.google.com/store/apps/details?id=com.aistudio.provalino.teacher.abcxyz",
    val playStorePackage: String = "com.aistudio.provalino.teacher.abcxyz"
)

object AppVersionChecker {
    private const val TAG = "AppVersionChecker"
    private const val DEFAULT_PACKAGE = "com.aistudio.provalino.teacher.abcxyz"
    private const val DEFAULT_UPDATE_URL = "https://play.google.com/store/apps/details?id=com.aistudio.provalino.teacher.abcxyz"

    fun getInstalledVersionCode(context: Context): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            40L
        }
    }

    fun getInstalledVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "40.0"
        } catch (e: Exception) {
            "40.0"
        }
    }

    suspend fun checkForUpdates(context: Context): AppUpdateState = withContext(Dispatchers.IO) {
        val currentVersionCode = getInstalledVersionCode(context)
        Log.d(TAG, "Installed Version Code: $currentVersionCode")

        try {
            val result = withTimeoutOrNull(3000L) {
                val firestore = FirebaseConfig.getFirestore() ?: return@withTimeoutOrNull null
                firestore.collection("app_config")
                    .document("version")
                    .get()
                    .await()
            }

            if (result != null && result.exists()) {
                val latestCode = result.getLong("latest_version_code")
                    ?: result.getLong("latest_version")
                    ?: currentVersionCode
                val minSupportedCode = result.getLong("min_supported_version")
                    ?: result.getLong("min_required_version_code")
                    ?: 1L
                val forceUpdateFlag = result.getBoolean("force_update") ?: false
                val latestName = result.getString("latest_version_name") ?: "40.0"
                val title = result.getString("update_title") ?: "Nova Versão do Provalino! 🚀"
                val releaseNotes = result.getString("release_notes") ?: ""
                val updateUrl = result.getString("update_url") ?: DEFAULT_UPDATE_URL
                val message = result.getString("update_message")
                    ?: if (releaseNotes.isNotBlank()) {
                        "Novidades nesta versão:\n$releaseNotes"
                    } else {
                        "Uma nova versão com melhorias e correções está disponível na Google Play Store. Atualize agora para continuar aproveitando!"
                    }
                val pkgName = result.getString("package_name") ?: DEFAULT_PACKAGE

                val isAvailable = latestCode > currentVersionCode
                val isForce = (currentVersionCode < minSupportedCode) || (isAvailable && forceUpdateFlag)

                AppUpdateState(
                    isUpdateAvailable = isAvailable || isForce,
                    isForceUpdate = isForce,
                    installedVersionCode = currentVersionCode,
                    latestVersionCode = latestCode,
                    latestVersionName = latestName,
                    updateTitle = title,
                    updateMessage = message,
                    releaseNotes = releaseNotes,
                    updateUrl = updateUrl,
                    playStorePackage = pkgName
                )
            } else {
                AppUpdateState(
                    installedVersionCode = currentVersionCode,
                    latestVersionCode = currentVersionCode,
                    latestVersionName = getInstalledVersionName(context)
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Informação da versão remota indisponível no momento: ${e.message}")
            AppUpdateState(
                installedVersionCode = currentVersionCode,
                latestVersionCode = currentVersionCode,
                latestVersionName = getInstalledVersionName(context)
            )
        }
    }
}
