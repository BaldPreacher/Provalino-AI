package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import java.text.Normalizer

/**
 * Modelo de dados para a coleção 'card_caa' do Firestore.
 * Representa cartões de Comunicação Aumentativa e Alternativa (CAA) para Educação Infantil e Inclusiva.
 *
 * Estrutura conforme especificação:
 * - base64: String (imagem codificada em Base64 ou 'nihil')
 * - categoria: String (ex: 'geral', 'alimentos', 'escola', etc.)
 * - imagem_url: String (URL da imagem ou 'nihil')
 * - mediaId: String (identificador de mídia ou 'nihil')
 * - mimType: String (tipo MIME, ex: 'image/png', 'image/jpeg' ou 'nihil')
 * - nivel_cognitivo: String (ex: 'inicial', 'intermediario', 'avancado')
 * - sinonimos: List<String> (array com palavras sinônimas associadas ao conceito)
 * - termo: String (palavra ou expressão chave do cartão, ex: 'teste exemplo')
 */
data class CardCaa(
    val id: String = "",
    val base64: String = "",
    val categoria: String = "geral",
    val imagem_url: String = "",
    val imagem_bw_url: String = "",
    val mediaId: String = "",
    val mimType: String = "",
    val nivel_cognitivo: String = "inicial",
    val sinonimos: List<String> = emptyList(),
    val termo: String = ""
) {
    /**
     * Verifica se há uma imagem Base64 válida vinculada ao cartão.
     */
    val hasValidBase64: Boolean
        get() = base64.isNotBlank() &&
                !base64.equals("nihil", ignoreCase = true) &&
                !base64.equals("null", ignoreCase = true) &&
                base64.length > 20

    /**
     * Verifica se há uma URL de imagem válida vinculada ao cartão.
     */
    val hasValidImageUrl: Boolean
        get() = imagem_url.isNotBlank() &&
                !imagem_url.equals("nihil", ignoreCase = true) &&
                !imagem_url.equals("null", ignoreCase = true) &&
                (imagem_url.startsWith("http://", ignoreCase = true) || imagem_url.startsWith("https://", ignoreCase = true))

    /**
     * Verifica se há uma URL de imagem em preto e branco (P&B) válida vinculada ao cartão.
     */
    val hasValidBwImageUrl: Boolean
        get() = imagem_bw_url.isNotBlank() &&
                !imagem_bw_url.equals("nihil", ignoreCase = true) &&
                !imagem_bw_url.equals("null", ignoreCase = true) &&
                (imagem_bw_url.startsWith("http://", ignoreCase = true) || imagem_bw_url.startsWith("https://", ignoreCase = true))

    /**
     * Converte a string Base64 em um Bitmap decodificado (se disponível).
     */
    fun decodeBase64Bitmap(): Bitmap? {
        if (!hasValidBase64) return null
        return try {
            val cleanBase64 = if (base64.contains("base64,")) {
                base64.substringAfter("base64,").trim()
            } else {
                base64.trim()
            }
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.w("CardCaa", "Erro ao decodificar Base64 do termo '$termo': ${e.message}")
            null
        }
    }

    /**
     * Compara um termo de busca com o termo principal e sinônimos do cartão,
     * ignorando acentos e maiúsculas/minúsculas.
     */
    fun matchesQuery(query: String): Boolean {
        val normalizedQuery = normalizeString(query)
        if (normalizedQuery.isBlank() || normalizedQuery.length < 2) return false

        val normalizedTermo = normalizeString(termo)
        if (normalizedTermo == normalizedQuery) {
            return true
        }

        // Permite match por palavras exatas
        val queryWords = normalizedQuery.split(" ").filter { it.isNotBlank() }
        val termoWords = normalizedTermo.split(" ").filter { it.isNotBlank() }
        if (termoWords.isNotEmpty() && queryWords.isNotEmpty()) {
            if (termoWords == queryWords) return true
            // Se o termo tem 1 palavra chave relevante (ex: 'arvore') e a query contém essa palavra como token completo
            if (termoWords.size == 1 && termoWords.first().length >= 3 && queryWords.contains(termoWords.first())) {
                return true
            }
            if (queryWords.size == 1 && queryWords.first().length >= 3 && termoWords.contains(queryWords.first())) {
                return true
            }
        }

        for (sin in sinonimos) {
            val normalizedSin = normalizeString(sin)
            if (normalizedSin == normalizedQuery) {
                return true
            }
            val sinWords = normalizedSin.split(" ").filter { it.isNotBlank() }
            if (sinWords.isNotEmpty() && queryWords.isNotEmpty()) {
                if (sinWords == queryWords) return true
                if (sinWords.size == 1 && sinWords.first().length >= 3 && queryWords.contains(sinWords.first())) {
                    return true
                }
            }
        }

        return false
    }

    companion object {
        fun normalizeString(text: String): String {
            val nfdNormalizedString = Normalizer.normalize(text.lowercase().trim(), Normalizer.Form.NFD)
            val pattern = Regex("\\p{InCombiningDiacriticalMarks}+")
            return pattern.replace(nfdNormalizedString, "")
                .replace(Regex("[^a-z0-9\\s]"), "")
                .trim()
        }

        /**
         * Mapeia com segurança um DocumentSnapshot do Firestore para o modelo CardCaa.
         */
        fun fromDocument(doc: DocumentSnapshot): CardCaa {
            val sinonimosList = when (val rawSin = doc.get("sinonimos")) {
                is List<*> -> rawSin.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
                is String -> rawSin.split(",", ";", "|").map { it.trim() }.filter { it.isNotBlank() }
                else -> emptyList()
            }

            val imgUrl = doc.getString("imagem_url")
                ?: doc.getString("imagemUrl")
                ?: doc.getString("url")
                ?: ""

            val imgBwUrl = doc.getString("imagem_bw_url")
                ?: doc.getString("imagemBwUrl")
                ?: doc.getString("imagem_bw")
                ?: doc.getString("bw_url")
                ?: ""

            return CardCaa(
                id = doc.id,
                base64 = doc.getString("base64") ?: "",
                categoria = doc.getString("categoria") ?: "geral",
                imagem_url = imgUrl,
                imagem_bw_url = imgBwUrl,
                mediaId = doc.getString("mediaId") ?: "",
                mimType = doc.getString("mimType") ?: "",
                nivel_cognitivo = doc.getString("nivel_cognitivo") ?: "inicial",
                sinonimos = sinonimosList,
                termo = doc.getString("termo") ?: ""
            )
        }

        /**
         * Converte o objeto CardCaa para Map pronto para persistência no Firestore.
         */
        fun toMap(card: CardCaa): Map<String, Any> {
            return mapOf(
                "base64" to card.base64,
                "categoria" to card.categoria,
                "imagem_url" to card.imagem_url,
                "imagem_bw_url" to card.imagem_bw_url,
                "mediaId" to card.mediaId,
                "mimType" to card.mimType,
                "nivel_cognitivo" to card.nivel_cognitivo,
                "sinonimos" to card.sinonimos,
                "termo" to card.termo
            )
        }
    }
}
