package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class CaaImportProgress(
    val isRunning: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val successCount: Int = 0,
    val errorCount: Int = 0,
    val currentTerm: String = "",
    val message: String = ""
)

object CaaJsonImporter {
    private const val TAG = "CaaJsonImporter"
    private const val COLLECTION_NAME = "card_caa"

    /**
     * Faz o download de uma imagem a partir de uma URL pública, redimensiona
     * para dimensões otimizadas de cartões CAA (max 256x256) e converte para Base64.
     */
    suspend fun downloadAndConvertImageToBase64(imageUrl: String): String? = withContext(Dispatchers.IO) {
        val cleanUrl = imageUrl.trim()
        if (cleanUrl.isBlank() || cleanUrl.equals("nihil", ignoreCase = true) || cleanUrl.equals("null", ignoreCase = true)) {
            return@withContext null
        }
        if (!cleanUrl.startsWith("http://", ignoreCase = true) && !cleanUrl.startsWith("https://", ignoreCase = true)) {
            return@withContext null
        }

        try {
            val url = URL(cleanUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 12000
            connection.readTimeout = 12000
            connection.instanceFollowRedirects = true
            connection.connect()

            val inputStream = connection.inputStream
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()

            if (originalBitmap == null) return@withContext null

            // Redimensionamento proporcional para caber em 256x256 (otimizado para cards CAA e economia no Firestore)
            val maxDimension = 256
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
                ratio
            } else 1.0f

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt().coerceAtLeast(1),
                    (height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            // Comprime em PNG de alta nitidez com transparência preservada
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao baixar imagem de '$cleanUrl': ${e.message}")
            null
        }
    }

    /**
     * Processa um JSON bruto contendo uma lista de cartões CAA e salva na coleção 'card_caa' do Firestore.
     * @param jsonText Conteúdo textual do arquivo JSON ou colado pelo desenvolvedor.
     * @param autoDownloadImages Se true, busca imagens de 'imagem_url' públicas e gera 'base64' caso 'base64' seja 'nihil'.
     * @param onProgress Callback invocado a cada cartão processado para atualizar a UI em tempo real.
     */
    suspend fun importJsonPipeline(
        jsonText: String,
        autoDownloadImages: Boolean = true,
        onProgress: (CaaImportProgress) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        val raw = jsonText.trim()
        if (raw.isBlank()) {
            return@withContext Result.failure(Exception("O conteúdo JSON fornecido está vazio."))
        }

        val jsonArray: JSONArray = try {
            if (raw.startsWith("[")) {
                JSONArray(raw)
            } else if (raw.startsWith("{")) {
                // Caso o usuário passe um único objeto JSON encapsulado
                val singleObj = JSONObject(raw)
                if (singleObj.has("cards") || singleObj.has("data") || singleObj.has("itens")) {
                    val key = if (singleObj.has("cards")) "cards" else if (singleObj.has("data")) "data" else "itens"
                    singleObj.getJSONArray(key)
                } else {
                    JSONArray().put(singleObj)
                }
            } else {
                return@withContext Result.failure(Exception("Formato JSON inválido. O texto deve iniciar com '[' ou '{'."))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Erro na sintaxe do JSON: ${e.localizedMessage}"))
        }

        val total = jsonArray.length()
        if (total == 0) {
            return@withContext Result.failure(Exception("Nenhum item encontrado no JSON fornecido."))
        }

        val firestore = FirebaseConfig.getFirestore()
            ?: return@withContext Result.failure(Exception("Firestore não está disponível no momento."))

        var successCount = 0
        var errorCount = 0

        onProgress(
            CaaImportProgress(
                isRunning = true,
                current = 0,
                total = total,
                successCount = 0,
                errorCount = 0,
                message = "Iniciando pipeline de importação..."
            )
        )

        val batch = firestore.batch()
        var batchCount = 0
        val collectionRef = firestore.collection(COLLECTION_NAME)

        for (i in 0 until total) {
            val itemObj = jsonArray.optJSONObject(i) ?: continue

            val termo = itemObj.optString("termo", "").trim()
            var base64 = itemObj.optString("base64", "nihil").trim()
            val categoria = itemObj.optString("categoria", "geral").trim()
            val imagemUrl = itemObj.optString("imagem_url", "nihil").trim()
            val mediaId = itemObj.optString("mediaId", "nihil").trim()
            val mimType = itemObj.optString("mimType", "nihil").trim()
            val nivelCognitivo = itemObj.optString("nivel_cognitivo", "inicial").trim()

            val sinonimosList = mutableListOf<String>()
            val sinonimosArray = itemObj.optJSONArray("sinonimos")
            if (sinonimosArray != null) {
                for (s in 0 until sinonimosArray.length()) {
                    val sin = sinonimosArray.optString(s, "").trim()
                    if (sin.isNotBlank()) sinonimosList.add(sin)
                }
            } else {
                val sinString = itemObj.optString("sinonimos", "")
                if (sinString.isNotBlank() && !sinString.equals("nihil", ignoreCase = true)) {
                    sinonimosList.addAll(sinString.split(",", ";").map { it.trim() }.filter { it.isNotBlank() })
                }
            }

            if (termo.isBlank()) {
                errorCount++
                continue
            }

            onProgress(
                CaaImportProgress(
                    isRunning = true,
                    current = i + 1,
                    total = total,
                    successCount = successCount,
                    errorCount = errorCount,
                    currentTerm = termo,
                    message = "Processando item ${i + 1}/$total: '$termo'..."
                )
            )

            // Se solicitado e não houver base64 válido, baixa imagem da URL pública
            if (autoDownloadImages && (base64.isBlank() || base64.equals("nihil", ignoreCase = true)) &&
                imagemUrl.isNotBlank() && !imagemUrl.equals("nihil", ignoreCase = true)
            ) {
                val downloadedB64 = downloadAndConvertImageToBase64(imagemUrl)
                if (downloadedB64 != null) {
                    base64 = downloadedB64
                }
            }

            val cardDoc = collectionRef.document()
            val cardData = mapOf(
                "base64" to base64,
                "categoria" to categoria.ifBlank { "geral" },
                "imagem_url" to imagemUrl.ifBlank { "nihil" },
                "mediaId" to mediaId.ifBlank { "nihil" },
                "mimType" to mimType.ifBlank { "nihil" },
                "nivel_cognitivo" to nivelCognitivo.ifBlank { "inicial" },
                "sinonimos" to sinonimosList,
                "termo" to termo
            )

            batch.set(cardDoc, cardData, SetOptions.merge())
            batchCount++
            successCount++

            // Limite de 450 operações por batch do Firestore
            if (batchCount >= 450) {
                try {
                    batch.commit().await()
                    batchCount = 0
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao gravar lote intermediário no Firestore: ${e.message}")
                }
            }
        }

        if (batchCount > 0) {
            try {
                batch.commit().await()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao gravar lote final no Firestore: ${e.message}")
                return@withContext Result.failure(e)
            }
        }

        // Recarrega o cache do repositório
        CardCaaRepository.getAllCards(forceRefresh = true)

        onProgress(
            CaaImportProgress(
                isRunning = false,
                current = total,
                total = total,
                successCount = successCount,
                errorCount = errorCount,
                currentTerm = "",
                message = "Importação concluída com sucesso! $successCount cartões adicionados/atualizados."
            )
        )

        return@withContext Result.success(successCount)
    }
}
