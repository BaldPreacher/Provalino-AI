package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

data class CloudProva(
    val id: String = "",
    val teacherId: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val turmaId: Int? = null,
    val questionKeys: List<String> = emptyList(),
    val instrucoesFormato: String = "",
    val dataCriacao: Long = System.currentTimeMillis()
)

object FirestoreRepository {
    private const val TAG = "FirestoreRepository"
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar FirebaseFirestore: ${e.message}")
            null
        }
    }

    /**
     * Gera uma chave única baseada no conteúdo da questão para deduplicação e economia de espaço.
     * Várias provas e professores podem usar as mesmas chaves de questões.
     */
    fun generateQuestionKey(questao: Questao): String {
        val raw = "${questao.enunciado.trim().lowercase()}_${questao.tipo}_${questao.respostaCorreta.trim().lowercase()}"
        return try {
            val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            raw.hashCode().toString()
        }
    }

    /**
     * Salva uma prova (atividade) na nuvem seguindo a arquitetura otimizada:
     * - As questões são armazenadas separadamente na coleção global "questoes_global" utilizando chaves de hash (deduplicação).
     * - A prova em si é salva na coleção "provas_cloud" contendo apenas metadados, instruções de formato e a lista de chaves (questionKeys).
     */
    suspend fun saveActivity(
        teacherId: String,
        titulo: String,
        descricao: String,
        turmaId: Int?,
        questoes: List<Questao>,
        instrucoesFormato: String = ""
    ): Result<String> {
        val firestore = db ?: return Result.failure(Exception("Firestore não está disponível."))
        if (teacherId.isBlank()) {
            return Result.failure(Exception("ID do professor não informado."))
        }
        if (questoes.isEmpty()) {
            return Result.failure(Exception("A prova deve conter pelo menos uma questão."))
        }

        try {
            val questionKeys = mutableListOf<String>()
            val batch = firestore.batch()
            val questoesRef = firestore.collection("questoes_global")

            for (q in questoes) {
                val key = generateQuestionKey(q)
                questionKeys.add(key)
                val docRef = questoesRef.document(key)
                val qMap = mapOf(
                    "id" to key,
                    "enunciado" to q.enunciado,
                    "tipo" to q.tipo,
                    "opcaoA" to q.opcaoA,
                    "opcaoB" to q.opcaoB,
                    "opcaoC" to q.opcaoC,
                    "opcaoD" to q.opcaoD,
                    "respostaCorreta" to q.respostaCorreta,
                    "assunto" to q.assunto,
                    "anoEscolar" to q.anoEscolar,
                    "perfilAdaptacao" to q.perfilAdaptacao,
                    "codigoBNCC" to q.codigoBNCC,
                    "pictogramasSuporte" to q.pictogramasSuporte
                )
                batch.set(docRef, qMap, SetOptions.merge())
            }

            // Confirma o lote de salvamento das questões compartilhadas
            batch.commit().await()

            // Salva a prova na coleção "provas_cloud" contendo apenas metadados e as chaves das questões
            val provaRef = firestore.collection("provas_cloud").document()
            val provaData = CloudProva(
                id = provaRef.id,
                teacherId = teacherId,
                titulo = titulo,
                descricao = descricao,
                turmaId = turmaId,
                questionKeys = questionKeys,
                instrucoesFormato = instrucoesFormato,
                dataCriacao = System.currentTimeMillis()
            )

            provaRef.set(provaData).await()
            val mainProfile = questoes.firstOrNull()?.perfilAdaptacao ?: "GERAL"
            AnalyticsRepository.logActivitySaved(mainProfile, questoes.size)
            return Result.success(provaRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar atividade no Firestore: ${e.message}", e)
            return Result.failure(e)
        }
    }

    /**
     * Recupera a biblioteca de provas/atividades salvas pelo professor na nuvem.
     */
    suspend fun getTeacherActivities(teacherId: String): Result<List<CloudProva>> {
        val firestore = db ?: return Result.failure(Exception("Firestore não está disponível."))
        if (teacherId.isBlank()) return Result.success(emptyList())

        try {
            val snapshot = firestore.collection("provas_cloud")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CloudProva::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.dataCriacao }

            return Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar atividades do professor: ${e.message}", e)
            return Result.failure(e)
        }
    }

    /**
     * Reconstitui a lista de questões detalhadas a partir de uma lista de chaves obtidas de uma prova.
     */
    suspend fun getQuestionsForKeys(questionKeys: List<String>): Result<List<Questao>> {
        val firestore = db ?: return Result.failure(Exception("Firestore não está disponível."))
        if (questionKeys.isEmpty()) return Result.success(emptyList())

        try {
            val questoesRef = firestore.collection("questoes_global")
            val questoes = mutableListOf<Questao>()

            // Firestore 'whereIn' suporta até 10 elementos por consulta. Processamos em lotes de 10.
            for (chunk in questionKeys.chunked(10)) {
                val snapshot = questoesRef.whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    questoes.add(
                        Questao(
                            id = 0,
                            enunciado = doc.getString("enunciado") ?: "",
                            tipo = doc.getString("tipo") ?: "MULTIPLE_CHOICE",
                            opcaoA = doc.getString("opcaoA") ?: "",
                            opcaoB = doc.getString("opcaoB") ?: "",
                            opcaoC = doc.getString("opcaoC") ?: "",
                            opcaoD = doc.getString("opcaoD") ?: "",
                            respostaCorreta = doc.getString("respostaCorreta") ?: "",
                            assunto = doc.getString("assunto") ?: "",
                            anoEscolar = doc.getString("anoEscolar") ?: "",
                            perfilAdaptacao = doc.getString("perfilAdaptacao") ?: "REGULAR",
                            codigoBNCC = doc.getString("codigoBNCC") ?: "",
                            pictogramasSuporte = doc.getString("pictogramasSuporte") ?: ""
                        )
                    )
                }
            }
            return Result.success(questoes)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar questões por chaves: ${e.message}", e)
            return Result.failure(e)
        }
    }

    /**
     * Remove uma prova salva na nuvem.
     */
    suspend fun deleteActivity(provaId: String): Result<Unit> {
        val firestore = db ?: return Result.failure(Exception("Firestore não está disponível."))
        try {
            firestore.collection("provas_cloud").document(provaId).delete().await()
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar atividade: ${e.message}", e)
            return Result.failure(e)
        }
    }
}
