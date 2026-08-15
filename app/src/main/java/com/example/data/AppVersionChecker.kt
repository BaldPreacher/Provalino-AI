package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppUpdateState(
    val isUpdateAvailable: Boolean = false,
    val isForceUpdate: Boolean = false,
    val installedVersionCode: Long = 0,
    val latestVersionCode: Long = 0,
    val latestVersionName: String = "",
    val updateTitle: String = "Nova Versão Disponível! 🚀",
    val updateMessage: String = "Existe uma versão mais recente do Provalino na Google Play Store. Atualize para ter acesso a novas melhorias e recursos das provas adaptadas.",
    val playStorePackage: String = "com.aistudio.provalino.teacher.abcxyz"
)

object AppVersionChecker {
    private const val TAG = "AppVersionChecker"
    private const val DEFAULT_PACKAGE = "com.aistudio.provalino.teacher.abcxyz"

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
            22L
        }
    }

    fun getInstalledVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "22.0"
        } catch (e: Exception) {
            "22.0"
        }
    }

    suspend fun checkForUpdates(context: Context): AppUpdateState {
        val currentVersionCode = getInstalledVersionCode(context)
        Log.d(TAG, "Installed Version Code: $currentVersionCode")

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docSnapshot = firestore.collection("app_config")
                .document("version")
                .get()
                .await()

            if (docSnapshot.exists()) {
                val latestCode = docSnapshot.getLong("latest_version_code") ?: currentVersionCode
                val minRequiredCode = docSnapshot.getLong("min_required_version_code") ?: currentVersionCode
                val latestName = docSnapshot.getString("latest_version_name") ?: "22.0"
                val title = docSnapshot.getString("update_title") ?: "Nova Versão do Provalino! 🚀"
                val message = docSnapshot.getString("update_message")
                    ?: "Uma nova versão com melhorias e correções está disponível na Google Play Store. Atualize agora para continuar aproveitando!"
                val pkgName = docSnapshot.getString("package_name") ?: DEFAULT_PACKAGE

                val isAvailable = latestCode > currentVersionCode
                val isForce = currentVersionCode < minRequiredCode

                AppUpdateState(
                    isUpdateAvailable = isAvailable,
                    isForceUpdate = isForce,
                    installedVersionCode = currentVersionCode,
                    latestVersionCode = latestCode,
                    latestVersionName = latestName,
                    updateTitle = title,
                    updateMessage = message,
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
