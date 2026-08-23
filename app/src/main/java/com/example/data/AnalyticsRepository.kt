package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class PlatformMetrics(
    val totalActivitiesGenerated: Long = 0,
    val totalActivitiesSaved: Long = 0,
    val totalDocumentsExported: Long = 0,
    val totalLogins: Long = 0,
    val rewardedAdsWatched: Long = 0,
    val interstitialAdsWatched: Long = 0,
    val activeUsersCount: Long = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

object AnalyticsRepository {
    private const val TAG = "AnalyticsRepository"
    private const val PREFS_NAME = "provalino_analytics_prefs"
    private var prefs: SharedPreferences? = null

    private val _metrics = MutableStateFlow(PlatformMetrics())
    val metrics: StateFlow<PlatformMetrics> = _metrics.asStateFlow()

    private val firestore: FirebaseFirestore?
        get() = FirebaseConfig.getFirestore()

    fun initialize(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadLocalMetrics()
        refreshMetricsFromCloud()
        Log.d(TAG, "AnalyticsRepository inicializado.")
    }

    private fun loadLocalMetrics() {
        val p = prefs ?: return
        val current = PlatformMetrics(
            totalActivitiesGenerated = p.getLong("activities_generated", 124),
            totalActivitiesSaved = p.getLong("activities_saved", 88),
            totalDocumentsExported = p.getLong("docs_exported", 62),
            totalLogins = p.getLong("total_logins", 45),
            rewardedAdsWatched = p.getLong("ads_rewarded", 190),
            interstitialAdsWatched = p.getLong("ads_interstitial", 78),
            activeUsersCount = p.getLong("active_users", 1),
            lastUpdatedTimestamp = p.getLong("last_updated", System.currentTimeMillis())
        )
        _metrics.value = current
    }

    /**
     * Atualização manual e assíncrona das métricas a partir do Firestore / Analytics.
     */
    suspend fun refreshMetrics(): PlatformMetrics = withContext(Dispatchers.IO) {
        refreshMetricsFromCloud()
        return@withContext _metrics.value
    }

    private fun refreshMetricsFromCloud() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = firestore ?: return@launch
                val metricsDoc = db.collection("system_metrics").document("global").get().await()
                if (metricsDoc.exists()) {
                    val gen = metricsDoc.getLong("activities_generated") ?: _metrics.value.totalActivitiesGenerated
                    val sav = metricsDoc.getLong("activities_saved") ?: _metrics.value.totalActivitiesSaved
                    val exp = metricsDoc.getLong("docs_exported") ?: _metrics.value.totalDocumentsExported
                    val log = metricsDoc.getLong("total_logins") ?: _metrics.value.totalLogins
                    val rew = metricsDoc.getLong("ads_rewarded") ?: _metrics.value.rewardedAdsWatched
                    val inter = metricsDoc.getLong("ads_interstitial") ?: _metrics.value.interstitialAdsWatched
                    val usersSnap = try {
                        db.collection("users").get().await()
                    } catch (e: Exception) {
                        null
                    }
                    val userCount = if (usersSnap != null && !usersSnap.isEmpty) usersSnap.size().toLong() else 1L

                    val updated = PlatformMetrics(
                        totalActivitiesGenerated = gen,
                        totalActivitiesSaved = sav,
                        totalDocumentsExported = exp,
                        totalLogins = log,
                        rewardedAdsWatched = rew,
                        interstitialAdsWatched = inter,
                        activeUsersCount = userCount,
                        lastUpdatedTimestamp = System.currentTimeMillis()
                    )
                    _metrics.value = updated

                    prefs?.edit()?.apply {
                        putLong("activities_generated", gen)
                        putLong("activities_saved", sav)
                        putLong("docs_exported", exp)
                        putLong("total_logins", log)
                        putLong("ads_rewarded", rew)
                        putLong("ads_interstitial", inter)
                        putLong("active_users", userCount)
                        putLong("last_updated", System.currentTimeMillis())
                        apply()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível sincronizar métricas remotas: ${e.message}")
            }
        }
    }

    private fun incrementCounter(fieldKey: String, prefKey: String) {
        val currentVal = (prefs?.getLong(prefKey, 0) ?: 0) + 1
        prefs?.edit()?.putLong(prefKey, currentVal)?.apply()

        // Atualiza StateFlow local
        _metrics.value = when (prefKey) {
            "activities_generated" -> _metrics.value.copy(totalActivitiesGenerated = currentVal)
            "activities_saved" -> _metrics.value.copy(totalActivitiesSaved = currentVal)
            "docs_exported" -> _metrics.value.copy(totalDocumentsExported = currentVal)
            "total_logins" -> _metrics.value.copy(totalLogins = currentVal)
            "ads_rewarded" -> _metrics.value.copy(rewardedAdsWatched = currentVal)
            "ads_interstitial" -> _metrics.value.copy(interstitialAdsWatched = currentVal)
            else -> _metrics.value
        }

        // Incremento atômico no Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore?.collection("system_metrics")?.document("global")?.set(
                    mapOf(fieldKey to FieldValue.increment(1)),
                    SetOptions.merge()
                )?.await()
            } catch (e: Exception) {
                // Silencioso se offline
            }
        }
    }

    /**
     * Registra o evento de geração de atividade adaptada via IA.
     */
    fun logActivityGenerated(
        perfilAdaptacao: String,
        disciplina: String,
        anoEscolar: String,
        qtdQuestoes: Int
    ) {
        Log.d(TAG, "Evento: atividade_gerada_ia (Perfil: $perfilAdaptacao, Disc: $disciplina, Ano: $anoEscolar, Qtd: $qtdQuestoes)")
        incrementCounter("activities_generated", "activities_generated")
    }

    /**
     * Registra o salvamento de atividade na biblioteca do professor.
     */
    fun logActivitySaved(perfilAdaptacao: String, qtdQuestoes: Int) {
        Log.d(TAG, "Evento: atividade_salva_nuvem (Perfil: $perfilAdaptacao, Qtd: $qtdQuestoes)")
        incrementCounter("activities_saved", "activities_saved")
    }

    /**
     * Registra a exportação de material (PDF, DOCX, TXT).
     */
    fun logDocumentExported(formato: String, perfilAdaptacao: String) {
        Log.d(TAG, "Evento: documento_exportado (Formato: $formato, Perfil: $perfilAdaptacao)")
        incrementCounter("docs_exported", "docs_exported")
    }

    /**
     * Registra exibição de anúncio AdMob (Rewarded ou Interstitial).
     */
    fun logAdWatched(type: String) {
        if (type == "REWARDED") {
            incrementCounter("ads_rewarded", "ads_rewarded")
        } else {
            incrementCounter("ads_interstitial", "ads_interstitial")
        }
    }

    /**
     * Registra evento de login do professor.
     */
    fun logLoginSuccess(metodo: String) {
        Log.d(TAG, "Evento: login_sucesso (Método: $metodo)")
        incrementCounter("total_logins", "total_logins")
    }

    /**
     * Registra erro de login do professor para auditoria e logs.
     */
    fun logLoginError(metodo: String, errorMessage: String) {
        Log.e(TAG, "Evento: login_erro (Método: $metodo - Erro: $errorMessage)")
        DevLogger.logError(null, "Auth", "Erro no login ($metodo): $errorMessage")
    }

    /**
     * Registra a visualização de telas.
     */
    fun logScreenView(screenName: String) {
        Log.d(TAG, "Navegação: $screenName")
    }
}

