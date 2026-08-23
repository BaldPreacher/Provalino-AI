package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfShareHelper {

    /**
     * Gera um arquivo PDF formatado em folha A4 para a Prova e abre o Intent de compartilhamento do Android.
     */
    fun shareExamAsPdf(
        context: Context,
        prova: Prova,
        turma: Turma?,
        questoes: List<Questao>,
        nomeEscola: String
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points (72 dpi)
            val pageHeight = 842 // A4 standard height in points (72 dpi)

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paintText = Paint().apply {
                color = Color.rgb(33, 33, 33)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val paintBold = Paint().apply {
                color = Color.rgb(20, 20, 20)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val paintTitle = Paint().apply {
                color = Color.rgb(30, 136, 229)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val paintHeader = Paint().apply {
                color = Color.rgb(50, 50, 50)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val paintLine = Paint().apply {
                color = Color.rgb(180, 180, 180)
                strokeWidth = 1f
            }

            val paintBox = Paint().apply {
                color = Color.rgb(245, 247, 250)
                style = Paint.Style.FILL
            }

            val paintBoxStroke = Paint().apply {
                color = Color.rgb(210, 215, 220)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val paintCaaBox = Paint().apply {
                color = Color.rgb(240, 253, 244)
                style = Paint.Style.FILL
            }

            val paintCaaBorder = Paint().apply {
                color = Color.rgb(134, 239, 172)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val paintCaaText = Paint().apply {
                color = Color.rgb(22, 101, 52)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val paintFooter = Paint().apply {
                color = Color.rgb(120, 120, 120)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }

            var currentY = 40f
            val marginX = 40f
            val contentWidth = pageWidth - (2 * marginX)

            fun drawHeader() {
                // Nome Escola & Logo Provalino
                canvas.drawText(nomeEscola.uppercase(), marginX, currentY, paintHeader)
                val logoText = "PROVALINO AI"
                val logoWidth = paintBold.measureText(logoText)
                canvas.drawText(logoText, pageWidth - marginX - logoWidth, currentY, paintTitle)

                currentY += 16f
                paintText.textSize = 10f
                canvas.drawText("AVALIAÇÃO ADAPTADA & INCLUSIVA", marginX, currentY, paintText)
                currentY += 12f
                canvas.drawLine(marginX, currentY, pageWidth - marginX, currentY, paintLine)
                currentY += 16f

                // Bloco de Identificação
                canvas.drawRoundRect(marginX, currentY, pageWidth - marginX, currentY + 36f, 6f, 6f, paintBox)
                canvas.drawRoundRect(marginX, currentY, pageWidth - marginX, currentY + 36f, 6f, 6f, paintBoxStroke)

                paintText.textSize = 9.5f
                canvas.drawText("Data: ____/____/_______", marginX + 10f, currentY + 15f, paintText)
                canvas.drawText("Turma: ${turma?.nome ?: "Geral"}", marginX + 160f, currentY + 15f, paintText)
                canvas.drawText("Matéria: ${turma?.materia ?: "Geral"}", marginX + 320f, currentY + 15f, paintText)
                canvas.drawText("Nome do Aluno: ____________________________________________________", marginX + 10f, currentY + 30f, paintText)

                currentY += 52f

                // Título da Prova
                val titleWidth = paintTitle.measureText(prova.titulo)
                val titleX = if (titleWidth < contentWidth) marginX + ((contentWidth - titleWidth) / 2f) else marginX
                canvas.drawText(prova.titulo, titleX, currentY, paintTitle)
                currentY += 24f
            }

            fun drawFooter() {
                val footerText = "Prova gerada pelo aplicativo Provalino - Atividades Adaptadas • Página $pageNumber"
                val footerWidth = paintFooter.measureText(footerText)
                canvas.drawLine(marginX, pageHeight - 35f, pageWidth - marginX, pageHeight - 35f, paintLine)
                canvas.drawText(footerText, marginX + ((contentWidth - footerWidth) / 2f), pageHeight - 22f, paintFooter)
            }

            fun checkNewPage(neededHeight: Float) {
                if (currentY + neededHeight > pageHeight - 50f) {
                    drawFooter()
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                    drawHeader()
                }
            }

            fun drawWrappedText(text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint): Float {
                var y = startY
                val words = text.split(" ")
                var currentLine = ""

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paint.measureText(testLine) > maxWidth) {
                        checkNewPage(16f)
                        canvas.drawText(currentLine, x, y, paint)
                        y += 14f
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) {
                    checkNewPage(16f)
                    canvas.drawText(currentLine, x, y, paint)
                    y += 14f
                }
                return y
            }

            drawHeader()

            // Questões
            questoes.forEachIndexed { index, q ->
                val isCaaProfile = q.perfilAdaptacao.uppercase() in listOf(
                    "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
                )
                val cleanSupport = PictogramInjector.cleanSupportText(q.pictogramasSuporte)
                val shouldShowCaa = isCaaProfile && cleanSupport.isNotBlank()

                // Espaço estimado da questão
                checkNewPage(65f)

                // Enunciado
                val questionHeader = "Questão ${index + 1}: ${q.enunciado}"
                currentY = drawWrappedText(questionHeader, marginX, currentY, contentWidth, paintBold)
                currentY += 4f

                // Suporte Visual CAA se realmente necessário
                if (shouldShowCaa) {
                    checkNewPage(24f)
                    val caaText = "🎨 Suporte Visual (CAA): $cleanSupport"
                    val caaWidth = Math.min(paintCaaText.measureText(caaText) + 16f, contentWidth)
                    canvas.drawRoundRect(marginX, currentY - 10f, marginX + caaWidth, currentY + 12f, 4f, 4f, paintCaaBox)
                    canvas.drawRoundRect(marginX, currentY - 10f, marginX + caaWidth, currentY + 12f, 4f, 4f, paintCaaBorder)
                    canvas.drawText(caaText, marginX + 8f, currentY + 4f, paintCaaText)
                    currentY += 20f
                }

                // Alternativas
                paintText.textSize = 10.5f
                if (q.tipo == "MULTIPLE_CHOICE") {
                    val opts = listOf("A" to q.opcaoA, "B" to q.opcaoB, "C" to q.opcaoC, "D" to q.opcaoD)
                    for ((letter, optText) in opts) {
                        if (optText.isNotBlank()) {
                            checkNewPage(15f)
                            val cleanOpt = formatOption(optText, letter)
                            canvas.drawText("    ($letter)  $cleanOpt", marginX + 10f, currentY, paintText)
                            currentY += 15f
                        }
                    }
                } else if (q.tipo == "TRUE_FALSE") {
                    checkNewPage(18f)
                    canvas.drawText("    (   ) Verdadeiro (V)          (   ) Falso (F)", marginX + 10f, currentY, paintText)
                    currentY += 18f
                } else {
                    checkNewPage(60f)
                    canvas.drawText("    [Resposta Dissertativa / Registro do Aluno]:", marginX + 10f, currentY, paintFooter)
                    currentY += 8f
                    canvas.drawRoundRect(marginX + 10f, currentY, pageWidth - marginX - 10f, currentY + 45f, 4f, 4f, paintBoxStroke)
                    currentY += 55f
                }

                currentY += 10f
                if (index < questoes.size - 1) {
                    canvas.drawLine(marginX, currentY, pageWidth - marginX, currentY, paintLine)
                    currentY += 14f
                }
            }

            drawFooter()
            pdfDocument.finishPage(page)

            // Salva o PDF no cacheDir do aplicativo
            val sanitizeName = prova.titulo.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "Prova_${sanitizeName}_${System.currentTimeMillis()}.pdf"
            val pdfFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Compartilhar via FileProvider
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Prova: ${prova.titulo}")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo a avaliação '${prova.titulo}' em PDF gerada pelo Provalino AI.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Prova em PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatOption(raw: String, prefixLetter: String): String {
        var cleaned = raw.trim()
        val prefixes = listOf(
            "$prefixLetter)", "$prefixLetter.", "$prefixLetter -",
            "${prefixLetter.lowercase()})", "${prefixLetter.lowercase()}.",
            "($prefixLetter)", "(${prefixLetter.lowercase()})"
        )
        for (p in prefixes) {
            if (cleaned.startsWith(p, ignoreCase = true)) {
                cleaned = cleaned.substring(p.length).trim()
            }
        }
        return cleaned
    }
}
