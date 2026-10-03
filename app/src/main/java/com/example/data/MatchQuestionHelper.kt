package com.example.data

object MatchQuestionHelper {

    fun isMatchQuestion(
        tipo: String,
        enunciado: String,
        opcaoA: String = "",
        opcaoB: String = "",
        opcaoC: String = "",
        opcaoD: String = ""
    ): Boolean {
        if (tipo.equals("MATCH", ignoreCase = true) || tipo.equals("PAREAMENTO", ignoreCase = true) || tipo.equals("LIGUE", ignoreCase = true)) {
            return true
        }
        val lowerEnunciado = enunciado.lowercase()
        val isLigueEnunciado = lowerEnunciado.contains("ligue") ||
                lowerEnunciado.contains("associe") ||
                lowerEnunciado.contains("pareie") ||
                lowerEnunciado.contains("relacione as colunas") ||
                lowerEnunciado.contains("faça a correspondência") ||
                lowerEnunciado.contains("ligar as colunas")

        val hasColumnKeywords = (opcaoA.contains("Coluna", ignoreCase = true) || opcaoA.contains("Coluna esquerda", ignoreCase = true)) &&
                (opcaoB.contains("Coluna", ignoreCase = true) || opcaoB.contains("Coluna direita", ignoreCase = true))

        val hasPipedOptions = opcaoA.contains("|") && opcaoB.contains("|")

        return isLigueEnunciado && (hasColumnKeywords || hasPipedOptions || isLigueEnunciado)
    }

    fun parseMatchColumns(
        opcaoA: String,
        opcaoB: String,
        opcaoC: String = "",
        opcaoD: String = "",
        enunciado: String = ""
    ): Pair<List<String>, List<String>> {
        val leftItems = mutableListOf<String>()
        val rightItems = mutableListOf<String>()

        val isBlockFormat = (opcaoA.contains("Coluna esquerda", ignoreCase = true) || opcaoA.contains("Coluna 1", ignoreCase = true) || opcaoA.contains("Coluna A", ignoreCase = true)) ||
                (opcaoB.contains("Coluna direita", ignoreCase = true) || opcaoB.contains("Coluna 2", ignoreCase = true) || opcaoB.contains("Coluna B", ignoreCase = true))

        if (isBlockFormat) {
            val cleanA = opcaoA.replace(Regex("""(?i)^A\)\s*"""), "")
                .replace(Regex("""(?i)^Coluna\s+(?:esquerda|1|A)\s*:\s*"""), "")
                .trim()
            val cleanB = opcaoB.replace(Regex("""(?i)^B\)\s*"""), "")
                .replace(Regex("""(?i)^Coluna\s+(?:direita|2|B)\s*:\s*"""), "")
                .trim()

            val rawLeft = cleanA.split(Regex("""\s*[|;]\s*|\n+""")).map { it.trim() }.filter { it.isNotBlank() }
            val rawRight = cleanB.split(Regex("""\s*[|;]\s*|\n+""")).map { it.trim() }.filter { it.isNotBlank() }

            leftItems.addAll(rawLeft)
            rightItems.addAll(rawRight)
        } else {
            val rawOptions = listOf(opcaoA, opcaoB, opcaoC, opcaoD).filter { it.isNotBlank() }
            val containsPipes = rawOptions.any { it.contains("|") || it.contains("->") || it.contains("—") }

            if (containsPipes) {
                for (opt in rawOptions) {
                    val clean = opt.replace(Regex("""^[A-D]\)\s*"""), "").trim()
                    val delimiter = when {
                        clean.contains("|") -> "|"
                        clean.contains("->") -> "->"
                        clean.contains("—") -> "—"
                        clean.contains("--->") -> "--->"
                        clean.contains(":") -> ":"
                        else -> null
                    }
                    if (delimiter != null) {
                        val parts = clean.split(delimiter)
                        if (parts.size >= 2) {
                            leftItems.add(parts[0].trim())
                            rightItems.add(parts.subList(1, parts.size).joinToString(" ").trim())
                        } else if (clean.isNotBlank()) {
                            leftItems.add(clean)
                        }
                    } else if (clean.isNotBlank()) {
                        leftItems.add(clean)
                    }
                }
            } else if (rawOptions.size >= 2) {
                // Se vieram opções sem pipes, agrupa a primeira metade na esquerda e segunda na direita se forem pares, ou coloca 1 a 1
                if (rawOptions.size == 4) {
                    leftItems.add(rawOptions[0].replace(Regex("""^[A-D]\)\s*"""), "").trim())
                    leftItems.add(rawOptions[1].replace(Regex("""^[A-D]\)\s*"""), "").trim())
                    rightItems.add(rawOptions[2].replace(Regex("""^[A-D]\)\s*"""), "").trim())
                    rightItems.add(rawOptions[3].replace(Regex("""^[A-D]\)\s*"""), "").trim())
                } else {
                    for ((idx, opt) in rawOptions.withIndex()) {
                        leftItems.add("Item ${idx + 1}: " + opt.replace(Regex("""^[A-D]\)\s*"""), "").trim())
                    }
                }
            }
        }

        // Fallbacks se alguma coluna ficar vazia
        if (leftItems.isEmpty()) {
            leftItems.add("Item 1")
            leftItems.add("Item 2")
        }
        if (rightItems.isEmpty()) {
            // Cria placeholders de ligação vazios correspondentes
            for (i in 1..leftItems.size) {
                rightItems.add("Opção correspondente $i")
            }
        }

        return Pair(leftItems, rightItems)
    }
}
