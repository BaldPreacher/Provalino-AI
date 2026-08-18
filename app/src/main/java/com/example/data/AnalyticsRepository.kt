package com.example.data

import android.content.Context
import android.util.Log

object AnalyticsRepository {
    private const val TAG = "AnalyticsRepository"

    fun initialize(context: Context) {
        Log.d(TAG, "AnalyticsRepository inicializado.")
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
    }

    /**
     * Registra o salvamento de atividade na biblioteca do professor.
     */
    fun logActivitySaved(perfilAdaptacao: String, qtdQuestoes: Int) {
        Log.d(TAG, "Evento: atividade_salva_nuvem (Perfil: $perfilAdaptacao, Qtd: $qtdQuestoes)")
    }

    /**
     * Registra a exportação de material (PDF, DOCX, TXT).
     */
    fun logDocumentExported(formato: String, perfilAdaptacao: String) {
        Log.d(TAG, "Evento: documento_exportado (Formato: $formato, Perfil: $perfilAdaptacao)")
    }

    /**
     * Registra evento de login do professor.
     */
    fun logLoginSuccess(metodo: String) {
        Log.d(TAG, "Evento: login_sucesso (Método: $metodo)")
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
