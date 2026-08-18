package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Repositório para consulta à API oficial do ARASAAC (Centro Aragonês de CAA).
 * Fornece fallback inteligente online com cache em memória/disco para termos
 * pedagógicos que não estejam no catálogo local imediato.
 */
object ArasaacRepository {

    private const val TAG = "ArasaacRepository"
    private const val BASE_SEARCH_URL = "https://api.arasaac.org/api/pictograms/pt/search/"
    private const val BASE_IMAGE_URL = "https://static.arasaac.org/pictograms/"

    // Cache em memória para evitar requisições repetidas durante a sessão
    private val memoryCache = ConcurrentHashMap<String, ArasaacPictogram?>()

    data class ArasaacPictogram(
        val id: Int,
        val keywords: List<String>,
        val imageUrl: String
    )

    /**
     * Busca o pictograma oficial no ARASAAC em português do Brasil.
     * Retorna o pictograma com a URL oficial da imagem em PNG de alta resolução.
     */
    suspend fun searchPictogram(term: String): ArasaacPictogram? = withContext(Dispatchers.IO) {
        val cleanTerm = term.lowercase().trim()
            .replace("[", "")
            .replace("]", "")
            .replace("pictograma:", "")
            .replace("imagem:", "")
            .replace("pictograma", "")
            .replace("imagem", "")
            .trim()

        if (cleanTerm.isBlank()) return@withContext null

        if (memoryCache.containsKey(cleanTerm)) {
            return@withContext memoryCache[cleanTerm]
        }

        try {
            val encodedTerm = URLEncoder.encode(cleanTerm, "UTF-8")
            val url = URL("$BASE_SEARCH_URL$encodedTerm")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3500
                readTimeout = 3500
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonText)

                if (jsonArray.length() > 0) {
                    val firstObj = jsonArray.getJSONObject(0)
                    val id = firstObj.getInt("_id")
                    val keywordsList = mutableListOf<String>()

                    if (firstObj.has("keywords")) {
                        val kwArr = firstObj.getJSONArray("keywords")
                        for (i in 0 until kwArr.length()) {
                            val kwObj = kwArr.getJSONObject(i)
                            if (kwObj.has("keyword")) {
                                keywordsList.add(kwObj.getString("keyword"))
                            }
                        }
                    }

                    // Gera a URL estática oficial de 300px do ARASAAC
                    val imgUrl = "$BASE_IMAGE_URL$id/${id}_300.png"
                    val result = ArasaacPictogram(id = id, keywords = keywordsList, imageUrl = imgUrl)
                    memoryCache[cleanTerm] = result
                    Log.d(TAG, "Pictograma ARASAAC encontrado para '$cleanTerm': ID $id -> $imgUrl")
                    return@withContext result
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                memoryCache[cleanTerm] = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ou timeout ao consultar API ARASAAC para '$cleanTerm': ${e.message}")
        }

        return@withContext null
    }
}
