package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.buildHtmlExamContent
import java.io.File
import java.io.FileOutputStream

object PdfShareHelper {

    private const val TAG = "PdfShareHelper"

    /**
     * Gera um arquivo PDF formatado em folha A4 utilizando o motor HTML5/CSS (idêntico ao botão Imprimir)
     * e dispara o Intent de compartilhamento do Android.
     */
    fun shareExamAsPdf(
        context: Context,
        prova: Prova,
        questoes: List<Questao>,
        nomeEscola: String,
        docType: String = "ATIVIDADE",
        fontSizeScale: String = "NORMAL",
        fontFamilyChoice: String = "PADRAO",
        highContrastMode: Boolean = false
    ) {
        val htmlContent = buildHtmlExamContent(
            prova = prova,
            questoes = questoes,
            escola = nomeEscola,
            docType = docType,
            fontSizeScale = fontSizeScale,
            fontFamilyChoice = fontFamilyChoice,
            highContrastMode = highContrastMode
        )
        shareHtmlAsPdf(context, htmlContent, prova.titulo)
    }

    /**
     * Atalho para compartilhar uma única atividade em PDF
     */
    fun shareSingleQuestionAsPdf(
        context: Context,
        questao: Questao,
        nomeEscola: String
    ) {
        val dummyProva = Prova(
            id = 0,
            titulo = "Atividade Adaptada - ${questao.assunto.ifBlank { "Educação Inclusiva" }}",
            descricao = "Atividade individual adaptada com recursos de apoio visual e DUA/AEE.",
            turmaId = 0,
            questoesIds = "${questao.id}",
            dataCriacao = System.currentTimeMillis()
        )
        val htmlContent = buildHtmlExamContent(dummyProva, listOf(questao), nomeEscola)
        shareHtmlAsPdf(context, htmlContent, dummyProva.titulo)
    }

    /**
     * Alias para compatibilidade com a chamada shareActivityAsPdf
     */
    fun shareActivityAsPdf(
        context: Context,
        questao: Questao,
        nomeEscola: String
    ) {
        shareSingleQuestionAsPdf(context, questao, nomeEscola)
    }

    private var activeWebView: WebView? = null

    /**
     * Converte uma string HTML5 formatada diretamente em um arquivo PDF A4 usando o renderizador HTML do WebView
     * e abre a janela de compartilhamento nativa do Android.
     */
    fun shareHtmlAsPdf(
        context: Context,
        htmlContent: String,
        title: String
    ) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                val cleanTitle = title.replace(Regex("""[^a-zA-Z0-9_\-]"""), "_").ifBlank { "Atividade" }
                val pdfDir = File(context.cacheDir, "provalino_pdfs").apply { mkdirs() }
                val pdfFile = File(pdfDir, "${cleanTitle}_Provalino.pdf")

                val targetWidth = 793 // A4 width in pixels (~96 dpi)

                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(android.graphics.Color.WHITE)
                }
                activeWebView = webView

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view ?: return
                        view.postDelayed({
                            try {
                                val contentHeight = (view.contentHeight * view.scale).toInt().coerceAtLeast(1000)
                                view.measure(
                                    android.view.View.MeasureSpec.makeMeasureSpec(targetWidth, android.view.View.MeasureSpec.EXACTLY),
                                    android.view.View.MeasureSpec.makeMeasureSpec(contentHeight, android.view.View.MeasureSpec.EXACTLY)
                                )
                                view.layout(0, 0, view.measuredWidth, view.measuredHeight)

                                val pdfDocument = PdfDocument()
                                val pageHeight = 1122 // A4 height proportional
                                val totalHeight = view.measuredHeight.coerceAtLeast(pageHeight)
                                var pageCount = (totalHeight + pageHeight - 1) / pageHeight
                                if (pageCount < 1) pageCount = 1

                                for (i in 0 until pageCount) {
                                    val pageInfo = PdfDocument.PageInfo.Builder(targetWidth, pageHeight, i + 1).create()
                                    val page = pdfDocument.startPage(pageInfo)
                                    val canvas = page.canvas

                                    canvas.save()
                                    canvas.translate(0f, -(i * pageHeight).toFloat())
                                    view.draw(canvas)
                                    canvas.restore()

                                    pdfDocument.finishPage(page)
                                }

                                val fos = FileOutputStream(pdfFile)
                                pdfDocument.writeTo(fos)
                                fos.close()
                                pdfDocument.close()

                                openPdfShareIntent(context, pdfFile, title)
                            } catch (e: Exception) {
                                Log.e(TAG, "Erro ao desenhar WebView em PDF, acionando fallback de impressão: ${e.message}", e)
                                fallbackToSystemPrintPdf(context, view, title)
                            } finally {
                                activeWebView = null
                            }
                        }, 500)
                    }
                }

                webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao iniciar compartilhamento PDF: ${e.message}", e)
                Toast.makeText(context, "Erro ao preparar PDF. Tente pelo botão Imprimir.", Toast.LENGTH_SHORT).show()
                activeWebView = null
            }
        }
    }

    private fun fallbackToSystemPrintPdf(context: Context, webView: WebView, title: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager != null) {
                val jobName = "${title.ifBlank { "Atividade" }} - Provalino"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    android.print.PrintAttributes.Builder()
                        .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                        .build()
                )
                Toast.makeText(context, "Selecione 'Salvar como PDF' na opção de impressora.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Não foi possível gerar o arquivo PDF neste dispositivo.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro no fallback de impressão: ${e.message}", e)
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPdfShareIntent(context: Context, pdfFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Avaliação/Atividade Adaptada: $title - Gerada via Provalino")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Atividade em PDF via:").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir share intent: ${e.message}", e)
            Toast.makeText(context, "Erro ao abrir compartilhamento: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
