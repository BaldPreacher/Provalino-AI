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
    val imagemBwUrl: String? = null,
    val categoria: String? = null,
    val nivelCognitivo: String? = null,
    val isCardCaa: Boolean = false,
    val isColoringContext: Boolean = false
)

/**
 * Injetor e processador de pictogramas para questões do Provalino.
 * Responsável por extrair marcações textuais como [Pictograma: ...] e [Imagem: ...],
 * resolver com a coleção 'card_caa' do Firestore, Catálogo Léxico Local e realizar fallback para a API do ARASAAC.
 */
object PictogramInjector {

    private const val TAG = "PictogramInjector"
    private val TAG_REGEX = Regex(
        """\[(?:Pictograma|Imagem|Foto|Fotografia|Desenho|Desenho\s+em\s+contorno|Desenho\s+em\s+contorno\s+para\s+colorir|Desenho\s+para\s+colorir|Espaço\s+para\s+desenho|Ilustração|Ilustracao|CAA|Card\s*CAA|Apoio\s*visual|Recurso\s*visual|Visual|Ícone|Icone|Símbolo|Simbolo|Card|Pinte)(?:[/\s\-_]+(?:Pictograma|Imagem|Foto|Fotografia|Desenho|Ilustração|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card))?:\s*([^\]]+)\]""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Identifica se um texto (enunciado, instrução ou campo) indica uma atividade de colorir/pintar/desenhar.
     */
    fun isColoringContext(contextText: String?): Boolean {
        if (contextText.isNullOrBlank()) return false
        val lower = contextText.lowercase()
        val keywords = listOf(
            "pinte", "pintar", "colorir", "colore", "desenhe", "desenhar",
            "preencha as cores", "pinta", "pintando", "colorindo", "traço", "traco",
            "contorno", "para colorir", "em contorno"
        )
        return keywords.any { lower.contains(it) }
    }

    /**
     * Desambilga palavras homógrafas e polissêmicas levando em conta grafia estrita com acentos e contexto do enunciado.
     */
    fun disambiguateTerm(term: String, contextText: String? = null): String {
        val raw = term.trim()
        val lower = raw.lowercase()
        val contextLower = (contextText ?: "").lowercase()

        // 1. país vs pais
        if (raw.equals("país", ignoreCase = false) || raw.equals("países", ignoreCase = false) ||
            contextLower.contains("geografia") || contextLower.contains("nação") || contextLower.contains("nacao") ||
            contextLower.contains("território") || contextLower.contains("territorio") || contextLower.contains("mapa") ||
            contextLower.contains("brasil") || contextLower.contains("continente") || contextLower.contains("capital")) {
            if (lower == "pais" || lower == "país" || lower == "paises" || lower == "países") {
                return "país"
            }
        }
        if (raw.equals("pais", ignoreCase = false) || contextLower.contains("família") || contextLower.contains("familia") ||
            contextLower.contains("pai e mãe") || contextLower.contains("meus pais") || contextLower.contains("responsáveis")) {
            if (lower == "pais" || lower == "país") {
                return "pais"
            }
        }

        // 2. manga (fruta) vs manga (camisa) vs mangá (hq)
        if (raw.equals("mangá", ignoreCase = false) || contextLower.contains("hq") || contextLower.contains("quadrinho") || contextLower.contains("japão")) {
            if (lower.contains("manga") || lower.contains("mangá")) return "mangá"
        }
        if (lower.contains("manga")) {
            if (contextLower.contains("camisa") || contextLower.contains("roupa") || contextLower.contains("vestido") || contextLower.contains("costura") || contextLower.contains("blusa")) {
                return "manga de camisa"
            }
            if (contextLower.contains("fruta") || contextLower.contains("comer") || contextLower.contains("suco") || contextLower.contains("árvore") || contextLower.contains("pomar") || contextLower.contains("doce")) {
                return "manga fruta"
            }
        }

        // 3. força vs forca
        if (raw.contains("força") || contextLower.contains("física") || contextLower.contains("fisica") || contextLower.contains("newton") || contextLower.contains("gravidade") || contextLower.contains("massa") || contextLower.contains("vetor")) {
            if (lower == "forca" || lower == "força") return "força"
        }
        if (raw == "forca" || contextLower.contains("jogo") || contextLower.contains("brincadeira") || contextLower.contains("adivinhar") || contextLower.contains("palavra")) {
            if (lower == "forca" || lower == "força") return "forca"
        }

        // 4. secretária vs secretaria
        if (raw.equals("secretária", ignoreCase = false) || contextLower.contains("profissão") || contextLower.contains("profissao") || contextLower.contains("trabalho")) {
            if (lower.contains("secretaria")) return "secretária"
        }
        if (raw.equals("secretaria", ignoreCase = false) || contextLower.contains("escola") || contextLower.contains("documento") || contextLower.contains("matrícula")) {
            if (lower.contains("secretaria")) return "secretaria"
        }

        // 5. sábio vs sabiá
        if (raw.equals("sabiá", ignoreCase = false) || contextLower.contains("pássaro") || contextLower.contains("passaro") || contextLower.contains("ave") || contextLower.contains("cantar")) {
            if (lower == "sabia" || lower == "sabiá" || lower == "sábio") return "sabiá"
        }
        if (raw.equals("sábio", ignoreCase = false) || contextLower.contains("inteligente") || contextLower.contains("mestre") || contextLower.contains("conhecimento")) {
            if (lower == "sabia" || lower == "sabiá" || lower == "sábio") return "sábio"
        }

        return raw
    }

    /**
     * Sanitiza um termo CAA, descartando frases de comando longas e retendo apenas conceitos e substantivos concretos.
     */
    fun sanitizeCaaTerm(term: String, contextText: String? = null): List<String> {
        val disambiguated = disambiguateTerm(term, contextText)
        val clean = disambiguated.trim()
            .replace("[", "")
            .replace("]", "")
            .replace("ARASAAC:", "")
            .trim()
        if (clean.isBlank()) return emptyList()

        val lower = clean.lowercase()

        // Mapeamentos específicos para expressões históricas e geopolíticas
        if (lower.contains("muro de berlim") || lower.contains("queda do muro")) {
            return listOf("MURO DE BERLIM")
        }
        if (lower == "otan" || lower.contains("otan ")) {
            return listOf("OTAN")
        }
        if (lower.contains("guerra fria")) {
            return listOf("GUERRA FRIA")
        }

        // Descarte de falsos cognatos matemáticos em questões de História / Português (ex: "sinal de divisão")
        if (lower.contains("sinal de divisão") || lower.contains("sinal de divisao")) {
            return emptyList()
        }

        // Descarte de datas e anos isolados (ex: "1989", "em 1989", "1500")
        if (lower.matches(Regex("""^(em\s+)?\d{4}$"""))) {
            return emptyList()
        }

        // Verifica se é uma frase de instrução/comando do professor (ex: "SOME AS QUANTIDADES...", "CONTE OS OBJETOS...")
        val isCommandPhrase = lower.startsWith("some ") || lower.startsWith("conte ") ||
                             lower.startsWith("calcule ") || lower.startsWith("leia ") ||
                             lower.startsWith("observe ") || lower.startsWith("marque ") ||
                             lower.contains("resposta correta") || lower.contains("verdadeiro ou falso") ||
                             lower.contains("marque a resposta") || clean.length > 25

        if (isCommandPhrase) {
            val extractedConcepts = mutableListOf<String>()
            val concreteKeywords = listOf(
                "soma" to "SOMA",
                "adição" to "ADIÇÃO",
                "subtração" to "SUBTRAÇÃO",
                "multiplicação" to "MULTIPLICAÇÃO",
                "maçã" to "MAÇÃ",
                "massa" to "MASSA",
                "estrela" to "ESTRELA",
                "fruta" to "FRUTA",
                "animal" to "ANIMAL",
                "número" to "NÚMERO",
                "contagem" to "CONTAGEM",
                "quantidade" to "CONTAGEM"
            )
            for ((key, concept) in concreteKeywords) {
                if (lower.contains(key) && !extractedConcepts.contains(concept)) {
                    extractedConcepts.add(concept)
                }
            }
            return extractedConcepts
        }

        if (isGenericOrBlacklisted(clean)) return emptyList()
        return listOf(clean.uppercase())
    }

    /**
     * Extrai termos de pictogramas e imagens a partir de uma String (enunciado, alternativas ou campo pictogramasSuporte).
     */
    fun extractTerms(text: String): List<String> {
        val matches = mutableListOf<String>()
        val tagMatches = TAG_REGEX.findAll(text)
        for (match in tagMatches) {
            var raw = match.groupValues[1].trim()
            raw = raw.replace(Regex("""\b(para\s+colorir|em\s+contorno|para\s+pintar|desenho\s+de|figura\s+de|ilustração\s+de)\b""", RegexOption.IGNORE_CASE), "").trim()
            matches.addAll(sanitizeCaaTerm(raw))
        }
        
        // Se não encontrou tags [Pictograma: ...], verifica se há termos no texto curto ou campo de suporte
        if (matches.isEmpty() && text.isNotBlank() && !text.contains("{")) {
            val tokens = text.split(",", ";", "|").map { it.trim() }.filter { it.isNotBlank() }
            for (token in tokens) {
                val cleanWord = token.replace(Regex("""[\p{So}\p{Sk}\p{Sm}\p{Cs}\p{Cn}]"""), "").trim()
                matches.addAll(sanitizeCaaTerm(cleanWord))
            }
        }
        return matches.distinct().filter { !isGenericOrBlacklisted(it) }
    }

    /**
     * Resolve uma lista de termos de pictogramas:
     * 1. Consulta a coleção 'card_caa' do Firestore (imagens personalizadas vinculadas no modelo JSON)
     * 2. Consulta o catálogo local (offline, imediato)
     * 3. Caso não encontre, consulta a API do ARASAAC (online fallback)
     */
    suspend fun resolveBadges(terms: List<String>, contextText: String? = null): List<PictogramBadge> {
        val badges = mutableListOf<PictogramBadge>()
        val coloringContext = isColoringContext(contextText) || terms.any { isColoringContext(it) }

        for (rawTerm in terms) {
            val clean = disambiguateTerm(rawTerm, contextText).trim()
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
                            imagemBwUrl = if (cardCaa.hasValidBwImageUrl) cardCaa.imagem_bw_url else null,
                            categoria = cardCaa.categoria,
                            nivelCognitivo = cardCaa.nivel_cognitivo,
                            isCardCaa = true,
                            isColoringContext = coloringContext
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
                }
                // Se não encontrou no ARASAAC nem no catálogo local/card_caa, NÃO adiciona badge genérico (🖼️)
            } catch (e: Exception) {
                Log.w(TAG, "Erro no fallback ARASAAC para '$clean': ${e.message}")
            }
        }

        return badges
    }

    /**
     * Injeta e padroniza os pictogramas em uma AIQuestao gerada pela IA.
     * Segue as diretrizes oficiais de CAA (Comunicação Aumentativa e Alternativa):
     * - Pictogramas densos e cartões visuais são aplicados apenas quando estritamente necessários e pertinentes.
     * - Não extrai alternativas ou termos genéricos para evitar poluição visual.
     */
    fun enrichAIQuestaoPictograms(questao: AIQuestao, requestedProfile: String = ""): AIQuestao {
        val extractedTerms = mutableListOf<String>()

        // 1. Coleta termos de tags explícitas de imagem / pictograma no enunciado (ex: [Desenho em contorno: Maçã], [Imagem: Sol])
        extractedTerms.addAll(extractTerms(questao.enunciado))

        // 2. Coleta termos já presentes no campo pictogramasSuporte se forem específicos
        if (questao.pictogramasSuporte.isNotBlank()) {
            val rawTerms = extractTerms(questao.pictogramasSuporte)
            for (t in rawTerms) {
                val clean = t.trim()
                if (!isGenericOrBlacklisted(clean)) {
                    extractedTerms.add(clean)
                }
            }
        }

        val isCaaTarget = requestedProfile.uppercase() in listOf(
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
        )

        // Se não for perfil CAA e não houver imagens explícitas extraídas, limpa o campo
        if (!isCaaTarget && extractedTerms.isEmpty()) {
            return questao.copy(
                pictogramasSuporte = "",
                enunciado = removeTechnicalTags(questao.enunciado)
            )
        }

        // Constrói a string de pictogramas enriquecida apenas com itens válidos e não genéricos
        val symbolsBuilder = StringBuilder()
        val uniqueTerms = extractedTerms.distinct().filter { !isGenericOrBlacklisted(it) }

        for (term in uniqueTerms) {
            val local = PictogramCatalog.find(term)
            if (local != null && local.symbol.isNotBlank() && local.symbol != "🖼️") {
                if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                symbolsBuilder.append("${local.symbol} ${local.label.uppercase()}")
            } else {
                val clean = term.replace("[", "").replace("]", "").replace("ARASAAC:", "").trim().uppercase()
                if (clean.isNotBlank() && !isGenericOrBlacklisted(clean)) {
                    val sym = PictogramCatalog.resolveSymbol(clean)
                    if (sym.isNotBlank() && sym != "🖼️") {
                        if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                        symbolsBuilder.append("$sym $clean")
                    } else if (clean.isNotBlank()) {
                        if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                        symbolsBuilder.append(clean)
                    }
                }
            }
        }

        val enrichedPictograms = symbolsBuilder.toString()

        return questao.copy(
            pictogramasSuporte = enrichedPictograms,
            enunciado = removeTechnicalTags(questao.enunciado)
        )
    }

    /**
     * Injeta e padroniza os pictogramas em uma Questão gerada pela IA,
     * garantindo suporte visual CAA e imagens de contorno/apoio.
     */
    fun enrichQuestaoPictograms(questao: Questao): Questao {
        val extractedTerms = mutableListOf<String>()

        // 1. Coleta termos de marcações explícitas no enunciado
        extractedTerms.addAll(extractTerms(questao.enunciado))

        // 2. Coleta termos do campo pictogramasSuporte se válidos
        if (questao.pictogramasSuporte.isNotBlank()) {
            val rawTerms = extractTerms(questao.pictogramasSuporte)
            for (t in rawTerms) {
                val clean = t.trim()
                if (!isGenericOrBlacklisted(clean)) {
                    extractedTerms.add(clean)
                }
            }
        }

        val isCaaTarget = questao.perfilAdaptacao.uppercase() in listOf(
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
        )

        if (!isCaaTarget && extractedTerms.isEmpty()) {
            return questao.copy(
                pictogramasSuporte = "",
                enunciado = removeTechnicalTags(questao.enunciado)
            )
        }

        // Constrói a string de pictogramas enriquecida
        val symbolsBuilder = StringBuilder()
        val uniqueTerms = extractedTerms.distinct().filter { !isGenericOrBlacklisted(it) }

        for (term in uniqueTerms) {
            val local = PictogramCatalog.find(term)
            if (local != null && local.symbol.isNotBlank() && local.symbol != "🖼️") {
                if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                symbolsBuilder.append("${local.symbol} ${local.label.uppercase()}")
            } else {
                val clean = term.replace("[", "").replace("]", "").replace("ARASAAC:", "").trim().uppercase()
                if (clean.isNotBlank() && !isGenericOrBlacklisted(clean)) {
                    val sym = PictogramCatalog.resolveSymbol(clean)
                    if (sym.isNotBlank() && sym != "🖼️") {
                        if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                        symbolsBuilder.append("$sym $clean")
                    } else if (clean.isNotBlank()) {
                        if (symbolsBuilder.isNotEmpty()) symbolsBuilder.append("  |  ")
                        symbolsBuilder.append(clean)
                    }
                }
            }
        }

        val enrichedPictograms = symbolsBuilder.toString()

        return questao.copy(
            pictogramasSuporte = enrichedPictograms,
            enunciado = removeTechnicalTags(questao.enunciado)
        )
    }

    private fun isGenericOrBlacklisted(term: String): Boolean {
        val lower = term.lowercase().trim()

        // Descarte de anos (ex: 1989, 1500) e datas em formato de 4 dígitos
        if (lower.matches(Regex("""^(em\s+)?\d{4}$"""))) return true

        // Descarte de termos matemáticos isolados em contextos ambíguos
        if (lower in listOf("sinal de divisão", "sinal de divisao", "sinais de divisão", "sinais de divisao")) return true

        if (lower in listOf(
            "verdadeiro", "falso", "verdadeiro (v)", "falso (f)", "(v)", "(f)", "v", "f",
            "sim", "não", "opcao a", "opcao b", "opcao c", "opcao d", "item", "imagem",
            "desenho", "foto", "fotografia", "geral", "calcule", "some", "conte", "marque",
            "leia", "observe", "resposta", "correta", "quantidade", "quantidades", "objeto",
            "objetos", "grupo", "grupos", "frase", "soma e marque", "resultado", "resolva",
            "cartoes c.", "cartões c.", "cartoes caa", "cartões caa", "cartao caa", "cartão caa",
            "cartão c.", "cartao c.", "duas colu.", "duas colunas", "duas col", "coluna", "colunas",
            "coluna a", "coluna b", "ligue", "pinte", "colorir", "espaço para desenho", "espaço para colorir",
            "espaco para desenho", "espaco para colorir", "espaço", "espaco", "desenho/colorir",
            "📚 ✏️", "✨ 🎯 📖", "📚", "✏️", "✨", "🎯", "📖", "📌"
        )) return true

        if (lower.contains("cartões c") || lower.contains("cartao c") || lower.contains("duas colu") ||
            lower.contains("duas colunas") || lower.contains("espaço para") || lower.contains("espaco para")) {
            return true
        }

        if (lower.length > 20 && (lower.contains("some") || lower.contains("conte") || lower.contains("calcule") || lower.contains("marque") || lower.contains("leia") || lower.contains("observe"))) {
            return true
        }

        return false
    }

    /**
     * Remove tags técnicas do enunciado para manter o texto limpo e legível para o aluno e na impressão.
     */
    fun removeTechnicalTags(text: String): String {
        return text
            .replace(TAG_REGEX, "")
            .replace(Regex("""\[ARASAAC:[^\]]+\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[(?:Orientação\s+ao\s+professor|Orientação|Orientacao|Instruções\s+de\s+aplicação|Instruções|Instrucao|Anotação|Anotacao|Observação|Observacao|Nota)[^\]]*\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[(?:Desenho\s+em\s+contorno|Desenho\s+para\s+colorir|Espaço\s+para\s+desenho|Espaço\s+para|Desenho|Ilustração|Ilustracao|Imagem|Foto|Pictograma|Apoio\s+visual|Recurso\s+visual|Card\s*CAA|CAA)[^\]]*\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s{2,}"""), " ")
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
        }.replace(TAG_REGEX) { matchResult ->
            val inner = matchResult.groupValues[1].trim()
            val found = PictogramCatalog.find(inner)
            if (found != null) "${found.symbol} ${found.label.uppercase()}" else "${PictogramCatalog.resolveSymbol(inner)} ${inner.uppercase()}"
        }
    }
}
