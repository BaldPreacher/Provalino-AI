package com.example.data

import kotlin.random.Random

object ActivityGridHelper {

    // --- DETECTION HELPERS ---

    fun isCrosswordQuestion(tipo: String, enunciado: String): Boolean {
        val t = tipo.uppercase().trim()
        if (t == "PALAVRAS_CRUZADAS" || t == "CROSSWORD" || t == "CRUZADINHA") return true
        val lower = enunciado.lowercase()
        return lower.contains("palavras cruzadas") ||
                lower.contains("palavra cruzada") ||
                lower.contains("cruzadinha")
    }

    fun isWordSearchQuestion(tipo: String, enunciado: String): Boolean {
        val t = tipo.uppercase().trim()
        if (t == "CACA_PALAVRAS" || t == "CAÇA_PALAVRAS" || t == "WORD_SEARCH" || t == "SOPA_LETRAS") return true
        val lower = enunciado.lowercase()
        return lower.contains("caça-palavras") ||
                lower.contains("caca palavras") ||
                lower.contains("caça palavras") ||
                lower.contains("sopa de letras") ||
                lower.contains("encontre as palavras no quadro")
    }

    // --- DATA MODELS ---

    data class CrosswordClue(
        val number: Int,
        val word: String,
        val clue: String,
        val isHorizontal: Boolean
    )

    data class CrosswordData(
        val grid: Array<Array<Char?>>, // null = inactive/black cell, Char = letter
        val cellNumbers: Array<Array<Int?>>, // number shown in top-left
        val horizontais: List<CrosswordClue>,
        val verticais: List<CrosswordClue>,
        val size: Int = 9
    )

    data class WordSearchData(
        val grid: List<List<Char>>,
        val words: List<String>,
        val size: Int = 9
    )

    // --- CAÇA-PALAVRAS (WORD SEARCH) GENERATOR ---

    fun generateWordSearch(
        opcaoA: String = "",
        opcaoB: String = "",
        opcaoC: String = "",
        opcaoD: String = "",
        enunciado: String = "",
        gridSize: Int = 9
    ): WordSearchData {
        val extractedWords = extractWords(opcaoA, opcaoB, opcaoC, opcaoD, enunciado)
        val validWords = if (extractedWords.isNotEmpty()) extractedWords else listOf("SOL", "LUA", "ESTRELA", "VIDA", "PAZ")

        val size = gridSize.coerceIn(8, 11)
        val matrix = Array(size) { Array(size) { ' ' } }
        val placedWords = mutableListOf<String>()

        val random = Random(enunciado.hashCode())

        // Place words either Horizontally (left-to-right) or Vertically (top-to-bottom)
        for (w in validWords) {
            val clean = sanitizeWord(w)
            if (clean.length > size || clean.length < 2) continue

            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                attempts++
                val isHoriz = attempts % 2 == 0
                if (isHoriz) {
                    val row = random.nextInt(size)
                    val col = random.nextInt(size - clean.length + 1)
                    var canPlace = true
                    for (i in clean.indices) {
                        val current = matrix[row][col + i]
                        if (current != ' ' && current != clean[i]) {
                            canPlace = false
                            break
                        }
                    }
                    if (canPlace) {
                        for (i in clean.indices) {
                            matrix[row][col + i] = clean[i]
                        }
                        placed = true
                        placedWords.add(clean)
                    }
                } else {
                    val row = random.nextInt(size - clean.length + 1)
                    val col = random.nextInt(size)
                    var canPlace = true
                    for (i in clean.indices) {
                        val current = matrix[row + i][col]
                        if (current != ' ' && current != clean[i]) {
                            canPlace = false
                            break
                        }
                    }
                    if (canPlace) {
                        for (i in clean.indices) {
                            matrix[row + i][col] = clean[i]
                        }
                        placed = true
                        placedWords.add(clean)
                    }
                }
            }
        }

        // Fill remaining spaces with random uppercase Portuguese alphabet letters
        val letters = "ABCDEILMNOPRSTUV"
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (matrix[r][c] == ' ') {
                    matrix[r][c] = letters[random.nextInt(letters.length)]
                }
            }
        }

        val listGrid = matrix.map { it.toList() }
        return WordSearchData(
            grid = listGrid,
            words = if (placedWords.isNotEmpty()) placedWords else validWords.map { sanitizeWord(it) },
            size = size
        )
    }

    // --- PALAVRAS CRUZADAS (CROSSWORD) GENERATOR ---

    fun generateCrossword(
        opcaoA: String = "",
        opcaoB: String = "",
        opcaoC: String = "",
        opcaoD: String = "",
        enunciado: String = "",
        gridSize: Int = 9
    ): CrosswordData {
        val pairs = extractWordCluePairs(opcaoA, opcaoB, opcaoC, opcaoD, enunciado)
        val validPairs = if (pairs.isNotEmpty()) pairs else listOf(
            "SOL" to "Estrela que ilumina e aquece nosso planeta Terra",
            "LUA" to "Satélite natural que brilha no céu à noite",
            "AGUA" to "Líquido vital essencial para a vida de plantas e animais",
            "ARVORE" to "Planta com tronco e folhas que nos dá sombra e oxigênio"
        )

        val size = gridSize.coerceIn(8, 10)
        val grid = Array(size) { Array<Char?>(size) { null } }
        val cellNums = Array(size) { Array<Int?>(size) { null } }

        val horizontais = mutableListOf<CrosswordClue>()
        val verticais = mutableListOf<CrosswordClue>()

        var currentNum = 1

        // Place first word horizontally in the center
        val first = validPairs[0]
        val firstClean = sanitizeWord(first.first).take(size)
        val startRow = size / 3
        val startCol = (size - firstClean.length) / 2
        for (i in firstClean.indices) {
            grid[startRow][startCol + i] = firstClean[i]
        }
        cellNums[startRow][startCol] = currentNum
        horizontais.add(CrosswordClue(currentNum, firstClean, first.second, isHorizontal = true))
        currentNum++

        // Try to intersect remaining words
        for (idx in 1 until validPairs.size) {
            val (rawWord, clue) = validPairs[idx]
            val word = sanitizeWord(rawWord).take(size)
            var placed = false

            // Try vertical intersection
            for (letterIdx in word.indices) {
                if (placed) break
                val targetLetter = word[letterIdx]

                for (r in 0 until size) {
                    if (placed) break
                    for (c in 0 until size) {
                        if (grid[r][c] == targetLetter) {
                            val vStartRow = r - letterIdx
                            val vCol = c
                            if (vStartRow >= 0 && vStartRow + word.length <= size) {
                                var fits = true
                                for (k in word.indices) {
                                    val checkRow = vStartRow + k
                                    val curr = grid[checkRow][vCol]
                                    if (curr != null && curr != word[k]) {
                                        fits = false
                                        break
                                    }
                                    // Check side neighbours if placing in empty cell
                                    if (curr == null) {
                                        if (vCol > 0 && grid[checkRow][vCol - 1] != null) fits = false
                                        if (vCol < size - 1 && grid[checkRow][vCol + 1] != null) fits = false
                                    }
                                }
                                if (fits) {
                                    for (k in word.indices) {
                                        grid[vStartRow + k][vCol] = word[k]
                                    }
                                    if (cellNums[vStartRow][vCol] == null) {
                                        cellNums[vStartRow][vCol] = currentNum
                                    }
                                    verticais.add(CrosswordClue(cellNums[vStartRow][vCol] ?: currentNum, word, clue, isHorizontal = false))
                                    currentNum++
                                    placed = true
                                }
                            }
                        }
                    }
                }
            }

            // Fallback placement horizontally in another empty row if no intersection
            if (!placed) {
                for (r in 0 until size) {
                    if (placed) break
                    val rowIsEmpty = (0 until size).all { grid[r][it] == null }
                    if (rowIsEmpty && r != startRow - 1 && r != startRow + 1) {
                        val cStart = (size - word.length).coerceAtLeast(0) / 2
                        if (cStart + word.length <= size) {
                            for (k in word.indices) {
                                grid[r][cStart + k] = word[k]
                            }
                            cellNums[r][cStart] = currentNum
                            horizontais.add(CrosswordClue(currentNum, word, clue, isHorizontal = true))
                            currentNum++
                            placed = true
                        }
                    }
                }
            }
        }

        return CrosswordData(
            grid = grid,
            cellNumbers = cellNums,
            horizontais = horizontais,
            verticais = verticais,
            size = size
        )
    }

    // --- HTML RENDERERS FOR PRINT / PDF ---

    fun renderWordSearchHtml(data: WordSearchData, highContrast: Boolean = false): String {
        val sb = StringBuilder()
        val borderColor = if (highContrast) "#000000" else "#334155"
        val cellBg = if (highContrast) "#FFFFC2" else "#FFFFFF"

        sb.append("<div style='text-align:center; margin: 12px 0 16px 0; page-break-inside: avoid;'>")
        sb.append("<table style='margin: 0 auto; border-collapse: collapse; border: 2px solid $borderColor;'>")
        for (row in data.grid) {
            sb.append("<tr>")
            for (ch in row) {
                sb.append("<td style='width: 28px; height: 28px; text-align: center; vertical-align: middle; border: 1.5px solid $borderColor; background-color: $cellBg; font-size: 15px; font-weight: bold; font-family: monospace, Courier, sans-serif; color: #000000;'>")
                sb.append(ch)
                sb.append("</td>")
            }
            sb.append("</tr>")
        }
        sb.append("</table>")

        // Words bank
        sb.append("<div style='margin-top: 12px; padding: 8px 12px; background-color: ${if (highContrast) "#FFFDE7" else "#F1F5F9"}; border-radius: 6px; border: 1.5px solid $borderColor; display: inline-block; max-width: 90%;'>")
        sb.append("<span style='font-size: 12px; font-weight: bold; color: #000000;'>🔍 Palavras para encontrar: </span>")
        sb.append("<span style='font-size: 12px; font-weight: bold; color: #0284C7; letter-spacing: 0.5px;'>")
        sb.append(data.words.joinToString(" • "))
        sb.append("</span>")
        sb.append("</div>")

        sb.append("</div>")
        return sb.toString()
    }

    fun renderCrosswordHtml(data: CrosswordData, highContrast: Boolean = false): String {
        val sb = StringBuilder()
        val borderColor = if (highContrast) "#000000" else "#1E293B"
        val activeBg = if (highContrast) "#FFFFC2" else "#FFFFFF"
        val inactiveBg = if (highContrast) "#475569" else "#334155"

        sb.append("<div style='margin: 12px 0 16px 0; page-break-inside: avoid;'>")
        sb.append("<table style='margin: 0 auto; border-collapse: collapse;'>")

        for (r in 0 until data.size) {
            sb.append("<tr>")
            for (c in 0 until data.size) {
                val letter = data.grid[r][c]
                val num = data.cellNumbers[r][c]

                if (letter != null) {
                    sb.append("<td style='width: 30px; height: 30px; text-align: center; vertical-align: middle; border: 1.5px solid $borderColor; background-color: $activeBg; position: relative; font-size: 14px; font-weight: bold;'>")
                    if (num != null) {
                        sb.append("<span style='position: absolute; top: 1px; left: 2px; font-size: 8.5px; font-weight: 800; color: #0284C7; line-height: 1;'>$num</span>")
                    }
                    sb.append("&nbsp;")
                    sb.append("</td>")
                } else {
                    sb.append("<td style='width: 30px; height: 30px; border: 1.5px solid ${if (highContrast) "#000000" else "#334155"}; background-color: $inactiveBg;'>&nbsp;</td>")
                }
            }
            sb.append("</tr>")
        }
        sb.append("</table>")

        // Clues box
        sb.append("<div style='margin-top: 12px; padding: 10px 14px; background-color: ${if (highContrast) "#FFFDE7" else "#F8FAFC"}; border: 1.5px solid $borderColor; border-radius: 8px;'>")
        sb.append("<div style='font-size: 12px; font-weight: bold; color: #0F766E; margin-bottom: 6px;'>🧩 DICAS DA CRUZADINHA:</div>")

        if (data.horizontais.isNotEmpty()) {
            sb.append("<div style='font-size: 11.5px; font-weight: bold; color: #1E293B; margin-top: 4px;'>👉 Horizontais:</div>")
            for (item in data.horizontais) {
                sb.append("<div style='font-size: 11px; margin-left: 10px; color: #000000;'><b>${item.number}.</b> ${item.clue} <i style='color: #64748B;'>(${item.word.length} letras)</i></div>")
            }
        }

        if (data.verticais.isNotEmpty()) {
            sb.append("<div style='font-size: 11.5px; font-weight: bold; color: #1E293B; margin-top: 6px;'>👇 Verticais:</div>")
            for (item in data.verticais) {
                sb.append("<div style='font-size: 11px; margin-left: 10px; color: #000000;'><b>${item.number}.</b> ${item.clue} <i style='color: #64748B;'>(${item.word.length} letras)</i></div>")
            }
        }

        sb.append("</div>")
        sb.append("</div>")
        return sb.toString()
    }

    // --- PARSING & SANITIZING UTILITIES ---

    private fun sanitizeWord(raw: String): String {
        return raw.uppercase()
            .replace(Regex("""[ÁÀÂÃÄ]"""), "A")
            .replace(Regex("""[ÉÈÊË]"""), "E")
            .replace(Regex("""[ÍÌÎÏ]"""), "I")
            .replace(Regex("""[ÓÒÔÕÖ]"""), "O")
            .replace(Regex("""[ÚÙÛÜ]"""), "U")
            .replace(Regex("""[Ç]"""), "C")
            .replace(Regex("""[^A-Z]"""), "")
            .trim()
    }

    private fun extractWords(
        opcaoA: String,
        opcaoB: String,
        opcaoC: String,
        opcaoD: String,
        enunciado: String
    ): List<String> {
        val list = mutableListOf<String>()

        // 1. Check options
        val options = listOf(opcaoA, opcaoB, opcaoC, opcaoD)
        for (opt in options) {
            val clean = opt.replace(Regex("""^[A-D]\)\s*"""), "").trim()
            if (clean.isNotBlank()) {
                val wordsInOpt = clean.split(Regex("""[,;|\n]+""")).map { sanitizeWord(it) }.filter { it.length in 2..10 }
                list.addAll(wordsInOpt)
            }
        }

        // 2. If options empty, extract from enunciado (words in quotes, or after 'Palavras:', etc.)
        if (list.isEmpty()) {
            val quotedMatches = Regex("""["']([a-zA-ZáàâãéèêíïóôõöúçÁÀÂÃÉÈÍÓÔÕÚÇ]{2,10})["']""").findAll(enunciado)
            for (m in quotedMatches) {
                list.add(sanitizeWord(m.groupValues[1]))
            }

            if (list.isEmpty() && enunciado.contains(":")) {
                val afterColon = enunciado.substringAfter(":")
                val wordsAfter = afterColon.split(Regex("""[,;|\s]+""")).map { sanitizeWord(it) }.filter { it.length in 2..10 }
                list.addAll(wordsAfter.take(6))
            }
        }

        return list.distinct().filter { it.isNotBlank() }
    }

    private fun extractWordCluePairs(
        opcaoA: String,
        opcaoB: String,
        opcaoC: String,
        opcaoD: String,
        enunciado: String
    ): List<Pair<String, String>> {
        val pairs = mutableListOf<Pair<String, String>>()
        val rawOptions = listOf(opcaoA, opcaoB, opcaoC, opcaoD).filter { it.isNotBlank() }

        for (opt in rawOptions) {
            val clean = opt.replace(Regex("""^[A-D]\)\s*"""), "").trim()
            val delimiter = when {
                clean.contains(":") -> ":"
                clean.contains("-") -> "-"
                clean.contains("—") -> "—"
                clean.contains("->") -> "->"
                else -> null
            }
            if (delimiter != null) {
                val parts = clean.split(delimiter)
                if (parts.size >= 2) {
                    val p1 = parts[0].trim()
                    val p2 = parts.subList(1, parts.size).joinToString(" ").trim()
                    // determine which is word and which is clue
                    if (p1.split(" ").size == 1 && p1.length <= 10) {
                        pairs.add(p1 to p2)
                    } else if (p2.split(" ").size == 1 && p2.length <= 10) {
                        pairs.add(p2 to p1)
                    } else {
                        pairs.add(p1.take(8) to p2)
                    }
                }
            }
        }

        return pairs
    }
}
