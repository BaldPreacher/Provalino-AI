package com.example.data

import android.util.Log

/**
 * Representa um item de pictograma individual renderizável na UI ou no PDF.
 */
data class PictogramBadge(
    val termo: String,
    val simboloLocal: String = "",
    val arasaacUrl: String? = null,
    val isArasaac: Boolean = false
)

/**
 * Injetor e processador de pictogramas para questões do Provalino.
 * Responsável por extrair marcações textuais como [Pictograma: ...] e [Imagem: ...],
 * resolver com o Catálogo Léxico Local e realizar fallback para a API do ARASAAC.
 */
object PictogramInjector {

    private const val TAG = "PictogramInjector"
    private val TAG_REGEX = Regex("""\[(?:Pictograma|Imagem|Foto|Desenho):\s*([^\]]+)\]""", RegexOption.IGNORE_CASE)

    /**
     * Extrai termos de pictogramas a partir de uma String (enunciado, alternativas ou campo pictogramasSuporte).
     */
    fun extractTerms(text: String): List<String> {
        val matches = TAG_REGEX.findAll(text).map { it.groupValues[1].trim() }.toMutableList()
        
        // Se não encontrou tags [Pictograma: ...], verifica se há termos separados por vírgula ou espaço no campo
        if (matches.isEmpty() && text.isNotBlank() && !text.contains("{") && text.length < 120) {
            val tokens = text.split(",", ";", "|").map { it.trim() }.filter { it.isNotBlank() }
            for (token in tokens) {
                // Remove emojis para pegar a palavra base
                val cleanWord = token.replace(Regex("""[\p{So}\p{Sk}\p{Sm}\p{Cs}\p{Cn}]"""), "").trim()
                if (cleanWord.isNotBlank()) {
                    matches.add(cleanWord)
                }
            }
        }
        return matches.distinct()
    }

    /**
     * Resolve uma lista de termos de pictogramas:
     * 1. Consulta o catálogo local (offline, imediato)
     * 2. Caso não encontre, consulta a API do ARASAAC (online fallback)
     */
    suspend fun resolveBadges(terms: List<String>): List<PictogramBadge> {
        val badges = mutableListOf<PictogramBadge>()

        for (rawTerm in terms) {
            val clean = rawTerm.trim()
            if (clean.isBlank()) continue

            // 1. Tenta catálogo local
            val localItem = PictogramCatalog.find(clean)
            if (localItem != null) {
                badges.add(
                    PictogramBadge(
                        termo = localItem.label.uppercase(),
                        simboloLocal = localItem.symbol,
                        arasaacUrl = null,
                        isArasaac = false
                    )
                )
                continue
            }

            // 2. Fallback para ARASAAC
            try {
                val arasaacResult = ArasaacRepository.searchPictogram(clean)
                if (arasaacResult != null) {
                    badges.add(
                        PictogramBadge(
                            termo = clean.uppercase(),
                            simboloLocal = "",
                            arasaacUrl = arasaacResult.imageUrl,
                            isArasaac = true
                        )
                    )
                } else {
                    // Fallback visual genérico caso não encontre nem no ARASAAC
                    badges.add(
                        PictogramBadge(
                            termo = clean.uppercase(),
                            simboloLocal = "🖼️",
                            arasaacUrl = null,
                            isArasaac = false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro no fallback ARASAAC para '$clean': ${e.message}")
                badges.add(
                    PictogramBadge(
                        termo = clean.uppercase(),
                        simboloLocal = "🖼️",
                        arasaacUrl = null,
                        isArasaac = false
                    )
                )
            }
        }

        return badges
    }

    /**
     * Injeta e padroniza os pictogramas em uma AIQuestao gerada pela IA.
     */
    fun enrichAIQuestaoPictograms(questao: AIQuestao, requestedProfile: String = ""): AIQuestao {
        val extractedTerms = mutableListOf<String>()

        // 1. Coleta termos das marcações do enunciado
        extractedTerms.addAll(extractTerms(questao.enunciado))

        // 2. Coleta termos das alternativas
        extractedTerms.addAll(extractTerms(questao.opcaoA))
        extractedTerms.addAll(extractTerms(questao.opcaoB))
        extractedTerms.addAll(extractTerms(questao.opcaoC))
        extractedTerms.addAll(extractTerms(questao.opcaoD))

        // 3. Coleta termos já presentes no campo pictogramasSuporte
        if (questao.pictogramasSuporte.isNotBlank()) {
            extractedTerms.addAll(extractTerms(questao.pictogramasSuporte))
        }

        // 4. Se a lista ainda estiver vazia e for perfil inclusivo, tenta inferir pelo assunto/palavras do enunciado
        val isHighSupport = requestedProfile in listOf("TEA", "AUTISMO", "DEF_INTELECTUAL", "TDAH", "SUPORTE_COGNITIVO", "SINDROME_DOWN")
        if (extractedTerms.isEmpty() && isHighSupport) {
            val words = questao.enunciado.split(" ", ",", ".", "?", "!")
            for (w in words) {
                val found = PictogramCatalog.find(w)
                if (found != null && !extractedTerms.contains(found.label)) {
                    extractedTerms.add(found.label)
                    if (extractedTerms.size >= 3) break
                }
            }
        }

        // Constrói a string de pictogramas enriquecida
        val symbolsBuilder = StringBuilder()
        val uniqueTerms = extractedTerms.distinct()

        for (term in uniqueTerms) {
            val local = PictogramCatalog.find(term)
            if (local != null) {
                if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                symbolsBuilder.append("${local.symbol} ${local.label.uppercase()}")
            } else {
                val clean = term.replace("[", "").replace("]", "").replace("ARASAAC:", "").trim().uppercase()
                if (clean.isNotBlank()) {
                    if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                    symbolsBuilder.append("📌 $clean")
                }
            }
        }

        val enrichedPictograms = if (symbolsBuilder.isNotEmpty()) {
            symbolsBuilder.toString()
        } else {
            cleanSupportText(questao.pictogramasSuporte)
        }

        return questao.copy(pictogramasSuporte = enrichedPictograms)
    }

    /**
     * Injeta e padroniza os pictogramas em uma Questão gerada pela IA,
     * garantindo que o campo pictogramasSuporte fique rico e estruturado.
     */
    fun enrichQuestaoPictograms(questao: Questao): Questao {
        val extractedTerms = mutableListOf<String>()

        // 1. Coleta termos das marcações do enunciado
        extractedTerms.addAll(extractTerms(questao.enunciado))

        // 2. Coleta termos das alternativas
        extractedTerms.addAll(extractTerms(questao.opcaoA))
        extractedTerms.addAll(extractTerms(questao.opcaoB))
        extractedTerms.addAll(extractTerms(questao.opcaoC))
        extractedTerms.addAll(extractTerms(questao.opcaoD))

        // 3. Coleta termos já presentes no campo pictogramasSuporte
        if (questao.pictogramasSuporte.isNotBlank()) {
            extractedTerms.addAll(extractTerms(questao.pictogramasSuporte))
        }

        // 4. Se a lista ainda estiver vazia e for perfil inclusivo, tenta inferir pelo assunto/palavras do enunciado
        if (extractedTerms.isEmpty() && questao.perfilAdaptacao in listOf("TEA", "AUTISMO", "DEF_INTELECTUAL", "TDAH", "SUPORTE_COGNITIVO", "SINDROME_DOWN")) {
            val words = questao.enunciado.split(" ", ",", ".", "?", "!")
            for (w in words) {
                val found = PictogramCatalog.find(w)
                if (found != null && !extractedTerms.contains(found.label)) {
                    extractedTerms.add(found.label)
                    if (extractedTerms.size >= 3) break
                }
            }
        }

        // Constrói a string de pictogramas enriquecida
        val symbolsBuilder = StringBuilder()
        val uniqueTerms = extractedTerms.distinct()

        for (term in uniqueTerms) {
            val local = PictogramCatalog.find(term)
            if (local != null) {
                if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                symbolsBuilder.append("${local.symbol} ${local.label.uppercase()}")
            } else {
                val clean = term.replace("[", "").replace("]", "").replace("ARASAAC:", "").trim().uppercase()
                if (clean.isNotBlank()) {
                    if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                    symbolsBuilder.append("📌 $clean")
                }
            }
        }

        val enrichedPictograms = if (symbolsBuilder.isNotEmpty()) {
            symbolsBuilder.toString()
        } else {
            cleanSupportText(questao.pictogramasSuporte)
        }

        return questao.copy(pictogramasSuporte = enrichedPictograms)
    }

    /**
     * Limpa qualquer resíduo técnico de suporte visual.
     */
    fun cleanSupportText(raw: String): String {
        if (raw.isBlank()) return ""
        return raw.replace(Regex("\\[ARASAAC:\\s*([^\\]]+)\\]", RegexOption.IGNORE_CASE)) { matchResult ->
            val inner = matchResult.groupValues[1].trim()
            val found = PictogramCatalog.find(inner)
            if (found != null) "${found.symbol} ${found.label.uppercase()}" else "📌 ${inner.uppercase()}"
        }.replace(Regex("\\[(Pictograma|Imagem):\\s*([^\\]]+)\\]", RegexOption.IGNORE_CASE)) { matchResult ->
            val inner = matchResult.groupValues[2].trim()
            val found = PictogramCatalog.find(inner)
            if (found != null) "${found.symbol} ${found.label.uppercase()}" else "📌 ${inner.uppercase()}"
        }
    }
}
