package com.example.data

import android.util.Log

/**
 * Representa um item de pictograma individual renderizável na UI ou no PDF.
 * Suporta imagens da coleção card_caa do Firestore (Base64 ou URL), ARASAAC e Catálogo Local.
 */
data class PictogramBadge(
    val termo: String,
    val simboloLocal: String = "",
    val arasaacUrl: String? = null,
    val isArasaac: Boolean = false,
    val base64Image: String? = null,
    val imageUrl: String? = null,
    val categoria: String? = null,
    val nivelCognitivo: String? = null,
    val isCardCaa: Boolean = false
)

/**
 * Injetor e processador de pictogramas para questões do Provalino.
 * Responsável por extrair marcações textuais como [Pictograma: ...] e [Imagem: ...],
 * resolver com a coleção 'card_caa' do Firestore, Catálogo Léxico Local e realizar fallback para a API do ARASAAC.
 */
object PictogramInjector {

    private const val TAG = "PictogramInjector"
    private val TAG_REGEX = Regex(
        """\[(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card)(?:[/\s\-_]+(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card))?:\s*([^\]]+)\]""",
        RegexOption.IGNORE_CASE
    )

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
     * 1. Consulta a coleção 'card_caa' do Firestore (imagens personalizadas vinculadas no modelo JSON)
     * 2. Consulta o catálogo local (offline, imediato)
     * 3. Caso não encontre, consulta a API do ARASAAC (online fallback)
     */
    suspend fun resolveBadges(terms: List<String>): List<PictogramBadge> {
        val badges = mutableListOf<PictogramBadge>()

        for (rawTerm in terms) {
            val clean = rawTerm.trim()
            if (clean.isBlank()) continue

            // 1. Tenta encontrar na coleção 'card_caa' do Firestore
            try {
                val cardCaa = CardCaaRepository.findCardByTerm(clean)
                if (cardCaa != null) {
                    val localMatch = PictogramCatalog.find(cardCaa.termo.ifBlank { clean })
                    badges.add(
                        PictogramBadge(
                            termo = (if (cardCaa.termo.isNotBlank()) cardCaa.termo else clean).uppercase(),
                            simboloLocal = localMatch?.symbol ?: "🧩",
                            arasaacUrl = null,
                            isArasaac = false,
                            base64Image = if (cardCaa.hasValidBase64) cardCaa.base64 else null,
                            imageUrl = if (cardCaa.hasValidImageUrl) cardCaa.imagem_url else null,
                            categoria = cardCaa.categoria,
                            nivelCognitivo = cardCaa.nivel_cognitivo,
                            isCardCaa = true
                        )
                    )
                    continue
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao buscar card_caa para '$clean': ${e.message}")
            }

            // 2. Tenta catálogo local
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

            // 3. Fallback para ARASAAC
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
     * Segue as diretrizes oficiais de CAA (Comunicação Aumentativa e Alternativa):
     * - Pictogramas densos e cartões visuais são aplicados apenas quando o perfil exigir suporte substancial (TEA, Suporte Cognitivo, Def. Intelectual).
     * - Para perfis Regulares, TDAH ou Dislexia, preserva-se o texto limpo com emoticons/emojis sem poluição visual.
     */
    fun enrichAIQuestaoPictograms(questao: AIQuestao, requestedProfile: String = ""): AIQuestao {
        val isCaaTarget = requestedProfile.uppercase() in listOf(
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
        )

        if (!isCaaTarget) {
            // Em perfis que não demandam CAA estruturada, remove tags artificiais mantendo texto limpo
            return questao.copy(
                pictogramasSuporte = "",
                enunciado = removeTechnicalTags(questao.enunciado)
            )
        }

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

        // 4. Se a lista ainda estiver vazia e for perfil de CAA, tenta inferir pelo assunto/palavras do enunciado
        if (extractedTerms.isEmpty()) {
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
                    val sym = PictogramCatalog.resolveSymbol(clean)
                    symbolsBuilder.append("$sym $clean")
                }
            }
        }

        val enrichedPictograms = if (symbolsBuilder.isNotEmpty()) {
            symbolsBuilder.toString()
        } else {
            cleanSupportText(questao.pictogramasSuporte)
        }

        return questao.copy(
            pictogramasSuporte = enrichedPictograms,
            enunciado = removeTechnicalTags(questao.enunciado)
        )
    }

    /**
     * Injeta e padroniza os pictogramas em uma Questão gerada pela IA,
     * garantindo suporte visual CAA apenas para perfis indicados.
     */
    fun enrichQuestaoPictograms(questao: Questao): Questao {
        val isCaaTarget = questao.perfilAdaptacao.uppercase() in listOf(
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
        )

        if (!isCaaTarget) {
            return questao.copy(
                pictogramasSuporte = "",
                enunciado = removeTechnicalTags(questao.enunciado)
            )
        }

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
        if (extractedTerms.isEmpty()) {
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
                    val sym = PictogramCatalog.resolveSymbol(clean)
                    symbolsBuilder.append("$sym $clean")
                }
            }
        }

        val enrichedPictograms = if (symbolsBuilder.isNotEmpty()) {
            symbolsBuilder.toString()
        } else {
            cleanSupportText(questao.pictogramasSuporte)
        }

        return questao.copy(
            pictogramasSuporte = enrichedPictograms,
            enunciado = removeTechnicalTags(questao.enunciado)
        )
    }

    /**
     * Remove tags técnicas do enunciado para manter o texto limpo e legível.
     */
    fun removeTechnicalTags(text: String): String {
        return text
            .replace(TAG_REGEX, "")
            .replace(Regex("""\[ARASAAC:[^\]]+\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[(?:Pictograma|Imagem|Foto|Desenho|CAA|Visual|Ícone|Icone)[^\]]*\]""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    /**
     * Limpa qualquer resíduo técnico de suporte visual.
     */
    fun cleanSupportText(raw: String): String {
        if (raw.isBlank()) return ""
        return raw.replace(Regex("""\[ARASAAC:\s*([^\]]+)\]""", RegexOption.IGNORE_CASE)) { matchResult ->
            val inner = matchResult.groupValues[1].trim()
            val found = PictogramCatalog.find(inner)
            if (found != null) "${found.symbol} ${found.label.uppercase()}" else "${PictogramCatalog.resolveSymbol(inner)} ${inner.uppercase()}"
        }.replace(Regex("""\[(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card)(?:[/\s\-_]+(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card))?:\s*([^\]]+)\]""", RegexOption.IGNORE_CASE)) { matchResult ->
            val inner = matchResult.groupValues[1].trim()
            val found = PictogramCatalog.find(inner)
            if (found != null) "${found.symbol} ${found.label.uppercase()}" else "${PictogramCatalog.resolveSymbol(inner)} ${inner.uppercase()}"
        }
    }
}
