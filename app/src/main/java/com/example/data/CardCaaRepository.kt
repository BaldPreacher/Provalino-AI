package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Repositório responsável pelo acesso e gerenciamento da coleção 'card_caa' no Firestore.
 * Oferece cache em memória veloz, normalização de termos para pesquisa pedagógica,
 * e suporte a consultas por termo, sinônimos, categoria e nível cognitivo.
 */
object CardCaaRepository {

    private const val TAG = "CardCaaRepository"
    private const val COLLECTION_NAME = "card_caa"

    private val db: FirebaseFirestore?
        get() = FirebaseConfig.getFirestore()

    // Cache em memória de todos os cartões carregados para acesso offline/imediato sem latência
    private val memoryCache = ConcurrentHashMap<String, CardCaa>()
    private var isPreloaded = false

    /**
     * Busca síncrona imediata na memória cache para renderização rápida em HTML e UI.
     */
    fun findCardFromCache(term: String): CardCaa? {
        val clean = term.trim()
        if (clean.isBlank()) return null
        val norm = CardCaa.normalizeString(clean)
        for (card in memoryCache.values) {
            if (CardCaa.normalizeString(card.termo) == norm) return card
        }
        for (card in memoryCache.values) {
            for (sin in card.sinonimos) {
                if (CardCaa.normalizeString(sin) == norm) return card
            }
        }
        for (card in memoryCache.values) {
            if (card.matchesQuery(clean)) return card
        }
        return null
    }

    /**
     * Retorna a lista completa de cartões CAA da coleção 'card_caa'.
     */
    suspend fun getAllCards(forceRefresh: Boolean = false): Result<List<CardCaa>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && isPreloaded && memoryCache.isNotEmpty()) {
            return@withContext Result.success(memoryCache.values.toList().sortedBy { it.termo })
        }

        val firestore = db ?: return@withContext Result.failure(Exception("Firestore indisponível"))

        try {
            val snapshot = firestore.collection(COLLECTION_NAME).get().await()
            val list = mutableListOf<CardCaa>()

            for (doc in snapshot.documents) {
                try {
                    val card = CardCaa.fromDocument(doc)
                    list.add(card)
                    memoryCache[doc.id] = card
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao processar documento ${doc.id} em card_caa: ${e.message}")
                }
            }

            isPreloaded = true
            Log.d(TAG, "Coleção card_caa carregada com sucesso: ${list.size} cartões.")
            return@withContext Result.success(list.sortedBy { it.termo })
        } catch (e: Exception) {
            Log.w(TAG, "Aviso ao buscar coleção card_caa do Firestore: ${e.message}")
            if (memoryCache.isNotEmpty()) {
                return@withContext Result.success(memoryCache.values.toList().sortedBy { it.termo })
            }
            return@withContext Result.failure(e)
        }
    }

    /**
     * Busca um cartão CAA que melhor corresponde a um termo específico
     * (verificando termo principal e sinônimos com normalização de acentos).
     */
    suspend fun findCardByTerm(term: String): CardCaa? = withContext(Dispatchers.IO) {
        val clean = term.trim()
        if (clean.isBlank()) return@withContext null

        val norm = CardCaa.normalizeString(clean)

        // 1. Tenta encontrar no cache em memória
        for (card in memoryCache.values) {
            if (CardCaa.normalizeString(card.termo) == norm) {
                return@withContext card
            }
        }
        for (card in memoryCache.values) {
            for (sin in card.sinonimos) {
                if (CardCaa.normalizeString(sin) == norm) {
                    return@withContext card
                }
            }
        }
        for (card in memoryCache.values) {
            if (card.matchesQuery(clean)) {
                return@withContext card
            }
        }

        // 2. Se não estiver no cache ou o cache não estiver pré-carregado, consulta Firestore
        val firestore = db ?: return@withContext null

        try {
            // Consulta direta por termo
            val termQuery = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("termo", clean)
                .limit(1)
                .get()
                .await()

            if (!termQuery.isEmpty) {
                val card = CardCaa.fromDocument(termQuery.documents[0])
                memoryCache[card.id] = card
                return@withContext card
            }

            // Consulta por array de sinônimos
            val sinQuery = firestore.collection(COLLECTION_NAME)
                .whereArrayContains("sinonimos", clean.lowercase())
                .limit(1)
                .get()
                .await()

            if (!sinQuery.isEmpty) {
                val card = CardCaa.fromDocument(sinQuery.documents[0])
                memoryCache[card.id] = card
                return@withContext card
            }

            // Se ainda não pré-carregou tudo, carrega a coleção e faz o match com normalização
            if (!isPreloaded) {
                val allResult = getAllCards()
                if (allResult.isSuccess) {
                    for (card in memoryCache.values) {
                        if (card.matchesQuery(clean)) {
                            return@withContext card
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao buscar cartão para termo '$clean' no Firestore: ${e.message}")
        }

        return@withContext null
    }

    /**
     * Busca cartões por filtro de texto, categoria e/ou nível cognitivo.
     */
    suspend fun searchCards(
        query: String = "",
        categoria: String? = null,
        nivelCognitivo: String? = null
    ): Result<List<CardCaa>> = withContext(Dispatchers.IO) {
        val allResult = getAllCards()
        if (allResult.isFailure && memoryCache.isEmpty()) {
            return@withContext allResult
        }

        var list = memoryCache.values.toList()

        if (!categoria.isNullOrBlank() && !categoria.equals("todos", ignoreCase = true)) {
            list = list.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }

        if (!nivelCognitivo.isNullOrBlank() && !nivelCognitivo.equals("todos", ignoreCase = true)) {
            list = list.filter { it.nivel_cognitivo.equals(nivelCognitivo, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            list = list.filter { it.matchesQuery(query) }
        }

        return@withContext Result.success(list.sortedBy { it.termo })
    }

    /**
     * Salva ou atualiza um cartão na coleção 'card_caa'.
     */
    suspend fun saveCard(card: CardCaa): Result<String> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext Result.failure(Exception("Firestore indisponível"))
        if (card.termo.isBlank()) {
            return@withContext Result.failure(Exception("O termo do cartão não pode ser vazio."))
        }

        try {
            val docRef = if (card.id.isNotBlank()) {
                firestore.collection(COLLECTION_NAME).document(card.id)
            } else {
                firestore.collection(COLLECTION_NAME).document()
            }

            val data = CardCaa.toMap(card)
            docRef.set(data, SetOptions.merge()).await()

            val savedCard = card.copy(id = docRef.id)
            memoryCache[docRef.id] = savedCard
            Log.d(TAG, "Cartão CAA salvo com sucesso: ${docRef.id} - ${card.termo}")
            return@withContext Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar cartão CAA no Firestore: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Exclui um cartão da coleção 'card_caa'.
     */
    suspend fun deleteCard(cardId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext Result.failure(Exception("Firestore indisponível"))
        try {
            firestore.collection(COLLECTION_NAME).document(cardId).delete().await()
            memoryCache.remove(cardId)
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar cartão CAA: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Limpa o cache em memória caso necessário.
     */
    fun clearCache() {
        memoryCache.clear()
        isPreloaded = false
    }
}
