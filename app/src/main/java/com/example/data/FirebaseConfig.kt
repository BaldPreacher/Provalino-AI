package com.example.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Configuração e inicialização centralizada do Firebase Firestore
 * com suporte ao ID de banco de dados específico do projeto.
 */
object FirebaseConfig {
    private const val TAG = "FirebaseConfig"
    
    // ID do banco de dados configurado no Firestore
    const val FIRESTORE_DATABASE_ID = "ai-studio-e6edd1d0-f89f-4ac1-9ac9-3b10db96cfc6"

    /**
     * Retorna a instância do FirebaseFirestore conectada ao banco de dados específico.
     * Possui fallback resiliente caso o app precise se conectar ao banco padrão.
     */
    fun getFirestore(): FirebaseFirestore? {
        return try {
            val app = FirebaseApp.getInstance()
            try {
                // Tenta conectar ao banco de dados nomeado
                FirebaseFirestore.getInstance(app, FIRESTORE_DATABASE_ID)
            } catch (e: Exception) {
                Log.w(TAG, "Tentando fallback para Firestore default: ${e.message}")
                FirebaseFirestore.getInstance(app)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro fatal ao inicializar FirebaseFirestore: ${e.message}")
            null
        }
    }
}
