package com.example.data

import android.content.Context
import android.util.Log
import com.aistudio.provalino.teacher.abcxyz.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
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
            val code = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            if (code > 0) code else BuildConfig.VERSION_CODE.toLong()
        } catch (e: Exception) {
            BuildConfig.VERSION_CODE.toLong()
        }
    }

    fun getInstalledVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            BuildConfig.VERSION_NAME
        }
    }

    private fun parseLongSafe(doc: DocumentSnapshot, vararg fields: String): Long? {
        for (field in fields) {
            val v = doc.get(field) ?: continue
            when (v) {
                is Number -> return v.toLong()
                is String -> {
                    val trimmed = v.trim()
                    trimmed.toDoubleOrNull()?.toLong()?.let { return it }
                    trimmed.toLongOrNull()?.let { return it }
                }
            }
        }
        return null
    }

    private fun parseBooleanSafe(doc: DocumentSnapshot, vararg fields: String): Boolean {
        for (field in fields) {
            val v = doc.get(field) ?: continue
            when (v) {
                is Boolean -> return v
                is String -> {
                    val lower = v.trim().lowercase()
                    if (lower == "true" || lower == "1" || lower == "sim" || lower == "yes") return true
                    if (lower == "false" || lower == "0" || lower == "nao" || lower == "não" || lower == "no") return false
                }
                is Number -> return v.toInt() == 1
            }
        }
        return false
    }

    private fun parseStringSafe(doc: DocumentSnapshot, vararg fields: String): String? {
        for (field in fields) {
            val v = doc.get(field) ?: continue
            val str = v.toString().trim()
            if (str.isNotBlank()) return str
        }
        return null
    }

    /**
     * Consulta o documento de versão no Firestore, procurando primeiro no banco nomeado do projeto
     * e, em caso de ausência, no banco padrão (default).
     */
    suspend fun checkForUpdates(context: Context): AppUpdateState = withContext(Dispatchers.IO) {
        val currentVersionCode = getInstalledVersionCode(context)
        Log.d(TAG, "Installed Version Code: $currentVersionCode")

        try {
            var docSnapshot: DocumentSnapshot? = null

            // 1. Tenta banco específico do projeto (se configurado)
            try {
                val firestoreNamed = FirebaseConfig.getFirestore()
                if (firestoreNamed != null) {
                    val res = withTimeoutOrNull(6000L) {
                        firestoreNamed.collection("app_config")
                            .document("version")
                            .get()
                            .await()
                    }
                    if (res != null && res.exists()) {
                        docSnapshot = res
                        Log.d(TAG, "Documento 'app_config/version' encontrado no banco nomeado: ${res.data}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao consultar banco nomeado: ${e.message}")
            }

            // 2. Fallback resiliente: Tenta o banco default do Firestore caso não encontrado
            if (docSnapshot == null || !docSnapshot.exists()) {
                try {
                    val app = FirebaseApp.getInstance()
                    val firestoreDefault = FirebaseFirestore.getInstance(app)
                    val resDefault = withTimeoutOrNull(6000L) {
                        firestoreDefault.collection("app_config")
                            .document("version")
                            .get()
                            .await()
                    }
                    if (resDefault != null && resDefault.exists()) {
                        docSnapshot = resDefault
                        Log.d(TAG, "Documento 'app_config/version' encontrado no banco default: ${resDefault.data}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Falha ao consultar banco default: ${e.message}")
                }
            }

            if (docSnapshot != null && docSnapshot.exists()) {
                val latestCode = parseLongSafe(
                    docSnapshot,
                    "latest_version_code", "latest_version", "version_code",
                    "latestVersionCode", "versionCode", "version"
                ) ?: currentVersionCode

                val minSupportedCode = parseLongSafe(
                    docSnapshot,
                    "min_supported_version", "min_required_version_code",
                    "min_version", "min_version_code", "minVersion"
                ) ?: 1L

                val forceUpdateFlag = parseBooleanSafe(
                    docSnapshot,
                    "force_update", "forceUpdate", "required"
                )

                val latestName = parseStringSafe(
                    docSnapshot,
                    "latest_version_name", "latest_version", "latestVersionName", "version_name", "versionName"
                ) ?: "${latestCode}.0"

                val title = parseStringSafe(
                    docSnapshot,
                    "update_title", "updateTitle", "title"
                ) ?: "Nova Versão do Provalino! 🚀"

                val releaseNotes = parseStringSafe(
                    docSnapshot,
                    "release_notes", "releaseNotes", "novidades", "changelog"
                ) ?: ""

                val updateUrl = parseStringSafe(
                    docSnapshot,
                    "update_url", "updateUrl", "url"
                ) ?: DEFAULT_UPDATE_URL

                val rawMsg = parseStringSafe(
                    docSnapshot,
                    "update_message", "updateMessage", "message"
                )
                val message = if (!rawMsg.isNullOrBlank()) {
                    rawMsg
                } else if (releaseNotes.isNotBlank()) {
                    "Novidades nesta versão:\n$releaseNotes"
                } else {
                    "Uma nova versão com melhorias e correções está disponível na Google Play Store. Atualize agora para continuar aproveitando!"
                }

                val pkgName = parseStringSafe(
                    docSnapshot,
                    "package_name", "packageName"
                ) ?: DEFAULT_PACKAGE

                val isAvailable = latestCode > currentVersionCode
                val isForce = (currentVersionCode < minSupportedCode) || (isAvailable && forceUpdateFlag)
                Log.d(TAG, "Versão instalada: $currentVersionCode, Versão remota: $latestCode ($latestName), Disponível: $isAvailable, Forçada: $isForce")

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
                Log.d(TAG, "Nenhum documento 'app_config/version' encontrado nos bancos.")
                AppUpdateState(
                    installedVersionCode = currentVersionCode,
                    latestVersionCode = currentVersionCode,
                    latestVersionName = getInstalledVersionName(context)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro geral ao verificar atualizações: ${e.message}", e)
            AppUpdateState(
                installedVersionCode = currentVersionCode,
                latestVersionCode = currentVersionCode,
                latestVersionName = getInstalledVersionName(context)
            )
        }
    }

    /**
     * Salva a configuração de versão no Firestore gravando em ambos os bancos
     * (nomeado e default) para garantir sincronização imediata com todos os usuários.
     */
    suspend fun saveVersionConfig(
        latestCode: Long,
        latestName: String,
        minCode: Long,
        forceUpdate: Boolean,
        releaseNotes: String,
        title: String = "Nova Versão do Provalino! 🚀",
        message: String = "",
        updateUrl: String = DEFAULT_UPDATE_URL
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = hashMapOf<String, Any>(
                "latest_version_code" to latestCode,
                "latest_version" to latestCode,
                "latest_version_name" to latestName,
                "min_supported_version" to minCode,
                "min_required_version_code" to minCode,
                "force_update" to forceUpdate,
                "update_title" to title,
                "release_notes" to releaseNotes,
                "update_message" to message.ifBlank {
                    if (releaseNotes.isNotBlank()) "Novidades nesta versão:\n$releaseNotes"
                    else "Uma nova versão com melhorias e correções está disponível na Google Play Store. Atualize agora para continuar aproveitando!"
                },
                "update_url" to updateUrl,
                "package_name" to DEFAULT_PACKAGE,
                "updated_at" to com.google.firebase.Timestamp.now()
            )

            var savedAtLeastOnce = false
            var lastError: Exception? = null

            // Salva no banco específico do projeto
            try {
                val firestoreNamed = FirebaseConfig.getFirestore()
                if (firestoreNamed != null) {
                    firestoreNamed.collection("app_config")
                        .document("version")
                        .set(data)
                        .await()
                    savedAtLeastOnce = true
                    Log.d(TAG, "Configuração salva com sucesso no banco nomeado.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao salvar versão no banco nomeado: ${e.message}")
                lastError = e
            }

            // Salva também no banco padrão (default) para máxima garantia
            try {
                val app = FirebaseApp.getInstance()
                val firestoreDefault = FirebaseFirestore.getInstance(app)
                firestoreDefault.collection("app_config")
                    .document("version")
                    .set(data)
                    .await()
                savedAtLeastOnce = true
                Log.d(TAG, "Configuração salva com sucesso no banco default.")
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao salvar versão no banco default: ${e.message}")
                if (lastError == null) lastError = e
            }

            if (savedAtLeastOnce) {
                Result.success(Unit)
            } else {
                Result.failure(lastError ?: Exception("Não foi possível conectar ao Firebase Firestore"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao gravar configuração de versão no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
}
