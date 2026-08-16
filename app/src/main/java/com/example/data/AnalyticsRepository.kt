package com.example.data

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsRepository {
    private const val TAG = "AnalyticsRepository"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                val app = com.google.firebase.FirebaseApp.getInstance()
                if (!app.options.applicationId.isNullOrEmpty()) {
                    if (firebaseAnalytics == null) {
                        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
                        Log.d(TAG, "Firebase Analytics inicializado com sucesso.")
                    }
                } else {
                    Log.d(TAG, "Firebase Analytics ignorado: google_app_id ausente.")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Firebase Analytics não disponível: ${e.message}")
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
        try {
            val bundle = Bundle().apply {
                putString("perfil_adaptacao", perfilAdaptacao.ifBlank { "GERAL" })
                putString("disciplina", disciplina.ifBlank { "GERAL" })
                putString("ano_escolar", anoEscolar.ifBlank { "NÃO_INFORMADO" })
                putInt("quantidade_questoes", qtdQuestoes)
            }
            firebaseAnalytics?.logEvent("atividade_gerada_ia", bundle)
            Log.d(TAG, "Evento registrado: atividade_gerada_ia ($perfilAdaptacao, $disciplina)")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar evento de atividade gerada: ${e.message}")
        }
    }

    /**
     * Registra o salvamento de atividade na biblioteca do professor.
     */
    fun logActivitySaved(perfilAdaptacao: String, qtdQuestoes: Int) {
        try {
            val bundle = Bundle().apply {
                putString("perfil_adaptacao", perfilAdaptacao.ifBlank { "GERAL" })
                putInt("quantidade_questoes", qtdQuestoes)
            }
            firebaseAnalytics?.logEvent("atividade_salva_nuvem", bundle)
            Log.d(TAG, "Evento registrado: atividade_salva_nuvem")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar evento de atividade salva: ${e.message}")
        }
    }

    /**
     * Registra a exportação de material (PDF, DOCX, TXT).
     */
    fun logDocumentExported(formato: String, perfilAdaptacao: String) {
        try {
            val bundle = Bundle().apply {
                putString("formato_exportacao", formato.uppercase())
                putString("perfil_adaptacao", perfilAdaptacao.ifBlank { "GERAL" })
            }
            firebaseAnalytics?.logEvent("documento_exportado", bundle)
            Log.d(TAG, "Evento registrado: documento_exportado ($formato)")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar evento de exportação: ${e.message}")
        }
    }

    /**
     * Registra evento de login do professor.
     */
    fun logLoginSuccess(metodo: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, metodo)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
            Log.d(TAG, "Evento registrado: login ($metodo)")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar evento de login: ${e.message}")
        }
    }

    /**
     * Registra a visualização de telas.
     */
    fun logScreenView(screenName: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar visualização de tela: ${e.message}")
        }
    }
}
