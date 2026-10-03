package com.example.data

import com.aistudio.provalino.teacher.abcxyz.BuildConfig
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.squareup.moshi.Types
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- GEMINI REST MODELS ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @param:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @param:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @param:Json(name = "responseMimeType") val responseMimeType: String? = null,
    @param:Json(name = "temperature") val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @param:Json(name = "contents") val contents: List<GeminiContent>,
    @param:Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @param:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @param:Json(name = "content") val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @param:Json(name = "candidates") val candidates: List<Candidate>? = null
)

// --- STRUCTURED OUTPUT OBJECTS ---

@JsonClass(generateAdapter = true)
data class AIQuestao(
    val enunciado: String,
    val tipo: String, // "MULTIPLE_CHOICE", "TRUE_FALSE", "DISCURSIVE"
    val opcaoA: String = "",
    val opcaoB: String = "",
    val opcaoC: String = "",
    val opcaoD: String = "",
    val respostaCorreta: String = "", // "A", "B", "C", "D", "V", "F", or sample text
    val assunto: String = "",
    val anoEscolar: String = "",
    val codigoBNCC: String = "",
    val pictogramasSuporte: String = ""
)

@JsonClass(generateAdapter = true)
data class AIQuestoesResponse(
    val questoes: List<AIQuestao>
)

// --- RETROFIT INTERFACE ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContentWithModel(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/gemini-3.5-flash-lite:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

@JsonClass(generateAdapter = true)
data class OpenAIChatMessage(
    @param:Json(name = "role") val role: String,
    @param:Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAIChatRequest(
    @param:Json(name = "model") val model: String,
    @param:Json(name = "messages") val messages: List<OpenAIChatMessage>,
    @param:Json(name = "temperature") val temperature: Double = 0.7
)

@JsonClass(generateAdapter = true)
data class OpenAIChoice(
    @param:Json(name = "message") val message: OpenAIChatMessage
)

@JsonClass(generateAdapter = true)
data class OpenAIChatResponse(
    @param:Json(name = "choices") val choices: List<OpenAIChoice>?
)

interface PollinationApiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAIChatRequest
    ): OpenAIChatResponse
}

class NetworkDiagnosticInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val url = request.url.toString()
        Log.d("ProvalinoNetwork", "--> [REQUEST] ${request.method} $url")

        val response: Response = try {
            chain.proceed(request)
        } catch (e: java.net.UnknownHostException) {
            Log.e("ProvalinoNetwork", "❌ [DNS_ERROR] Failed to resolve host for $url. Check internet connection or DNS settings.", e)
            throw e
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("ProvalinoNetwork", "⏰ [TIMEOUT_ERROR] Connection or read timeout for $url after ${System.currentTimeMillis() - startTime}ms.", e)
            throw e
        } catch (e: java.net.ConnectException) {
            Log.e("ProvalinoNetwork", "🔌 [CONNECT_ERROR] Connection refused or unreachable for $url.", e)
            throw e
        } catch (e: javax.net.ssl.SSLException) {
            Log.e("ProvalinoNetwork", "🔒 [SSL_ERROR] SSL Handshake failed for $url.", e)
            throw e
        } catch (e: java.io.IOException) {
            Log.e("ProvalinoNetwork", "🌐 [IO_ERROR] Network IO error for $url: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("ProvalinoNetwork", "💥 [UNKNOWN_NETWORK_ERROR] Unexpected error for $url", e)
            throw e
        }

        val duration = System.currentTimeMillis() - startTime
        Log.d("ProvalinoNetwork", "<-- [RESPONSE] HTTP ${response.code} ${response.message} for $url in ${duration}ms")

        if (!response.isSuccessful) {
            val errorBody = response.peekBody(4096).string()
            Log.e("ProvalinoNetwork", "⚠️ [API_ERROR_RESPONSE] HTTP ${response.code}: $errorBody")
        }

        return response
    }
}

// --- CLIENT SETUP ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(NetworkDiagnosticInterceptor())
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    private suspend fun callPollinationsFallback(systemPrompt: String, userPrompt: String): String? = withContext(Dispatchers.IO) {
        val rawPollinationKey = try {
            val f = BuildConfig::class.java.getField("POLLINATION_API_KEY")
            f.get(null) as? String ?: ""
        } catch (e: Exception) {
            try {
                val f2 = BuildConfig::class.java.getField("POLLINATION")
                f2.get(null) as? String ?: ""
            } catch (e2: Exception) {
                ""
            }
        }
        val pollinationKey = if (rawPollinationKey.isNotBlank() && rawPollinationKey != "MY_POLLINATION_API_KEY") {
            rawPollinationKey
        } else {
            "pollination"
        }

        val authHeader = "Bearer $pollinationKey"

        val pollinationRetrofit = Retrofit.Builder()
            .baseUrl("https://gen.pollinations.ai/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val pollinationService = pollinationRetrofit.create(PollinationApiService::class.java)

        val modelsToTry = listOf("gpt-5.6-luna", "openai-fast", "mistral")
        for (modelName in modelsToTry) {
            try {
                Log.d("ProvalinoPollination", "Invocando fallback Pollinations AI com modelo $modelName")
                val request = OpenAIChatRequest(
                    model = modelName,
                    messages = listOf(
                        OpenAIChatMessage(role = "system", content = "$systemPrompt\nResponda ESTRITAMENTE em formato JSON válido."),
                        OpenAIChatMessage(role = "user", content = userPrompt)
                    ),
                    temperature = 0.7
                )
                val response = pollinationService.chatCompletions(authHeader, request)
                val content = response.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    return@withContext content
                }
            } catch (e: Exception) {
                Log.e("ProvalinoPollination", "Pollinations AI tentativa com modelo $modelName falhou: ${e.message}")
            }
        }
        null
    }

    /**
     * Mapeia o ano escolar para a faixa etária correspondente e gera diretrizes pedagógicas estritas de linguagem.
     */
    fun getAgeRangeAndGuidelinesForGrade(grade: String): Pair<String, String> {
        val clean = grade.lowercase().trim()
        return when {
            clean.contains("infantil") || clean.contains("creche") || clean.contains("pré") || clean.contains("pre") -> {
                Pair(
                    "3 a 5 anos (Educação Infantil)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (3 a 5 anos - Educação Infantil):
                    - Fase Pré-Operatória: pensamento concreto, afeto e oralidade.
                    - LINGUAGEM MANDATÓRIA: Extremamente afetuosa, carinhosa, lúdica e acolhedora.
                    - Frases ultracurtas (3 a 6 palavras simples). Use comandos afetuosos: 'Veja a figura', 'Aponte o gatinho', 'Pinte a frutinha'.
                    - ZERO de termos acadêmicos ou burocráticos ('Assinale a alternativa correta', 'Marque o item condizente').
                    - NUNCA use 'Verdadeiro (V) / Falso (F)'! Se for questão binária, use apenas 'Sim 😊' ou 'Não 🙁'.
                    - Universo infantil: animais, brinquedos, frutas, cores e cantigas.
                    """.trimIndent()
                )
            }
            clean.contains("1º") || clean.contains("1o") || clean.contains("primeiro") -> {
                Pair(
                    "6 anos (1º Ano do Ensino Fundamental - Alfabetização Inicial)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (6 anos - 1º Ano do Ensino Fundamental):
                    - A criança tem cerca de 6 anos e está no início da alfabetização e letramento.
                    - LINGUAGEM MANDATÓRIA: Linguagem afetiva, carinhosa, acolhedora e de fácil compreensão pela criança de 6 anos.
                    - Enunciados de no máximo 1 frase curta e direta (até 8 a 10 palavras simples).
                    - PROIBIDO uso de linguagem fria de concurso ou vestibular ('Assinale a alternativa correta', 'Avalie a asserção', 'Identifique a opção condizente').
                    - Use comandos infantis acolhedores: 'Qual é a letra?', 'Ligue as figuras iguais', 'Pinte as estrelinhas'.
                    - NUNCA use 'Verdadeiro (V) / Falso (F)' para crianças de 6 anos! Em vez disso, use opções claras como 'Sim 😊' e 'Não 🙁' ou 'Certo ✅' e 'Errado ❌'.
                    - Vocabulário familiar do universo infantil: bola, gato, sol, casa, boneca, escola, pipoca, uva.
                    """.trimIndent()
                )
            }
            clean.contains("2º") || clean.contains("2o") || clean.contains("segundo") -> {
                Pair(
                    "7 anos (2º Ano do Ensino Fundamental - Alfabetização e Letramento)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (7 anos - 2º Ano do Ensino Fundamental):
                    - Idade aproximada: 7 anos. Alfabetização em consolidação.
                    - Sentenças curtas na ordem direta (Sujeito + Verbo + Predicado).
                    - LINGUAGEM MANDATÓRIA: Acolhedora, clara e incentivadora.
                    - Evite siglas abstratas como 'V' e 'F'; prefira 'Certo ✅ / Errado ❌' ou 'Sim / Não'.
                    - Situações-problema cotidianas, brincadeiras, família e pequenos animais.
                    """.trimIndent()
                )
            }
            clean.contains("3º") || clean.contains("3o") || clean.contains("terceiro") -> {
                Pair(
                    "8 anos (3º Ano do Ensino Fundamental)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (8 anos - 3º Ano do Ensino Fundamental):
                    - Idade aproximada: 8 anos. Pensamento operatório concreto.
                    - Enunciados objetivos, sem ambiguidades e sem textos desnecessariamente longos.
                    - Contextualização lúdica e prática com a rotina escolar e familiar.
                    """.trimIndent()
                )
            }
            clean.contains("4º") || clean.contains("4o") || clean.contains("quarto") || clean.contains("5º") || clean.contains("5o") || clean.contains("quinto") -> {
                Pair(
                    "9 a 10 anos (4º e 5º Ano do Ensino Fundamental)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (9 a 10 anos - 4º e 5º Ano do Ensino Fundamental):
                    - Transição do pensamento concreto para o raciocínio lógico estruturado.
                    - Comandos claros, objetivos e contextualizados com situações reais da vida do estudante.
                    - Linguagem acessível sob os princípios do DUA, evitando sobrecarga cognitiva e enunciados prolixos.
                    """.trimIndent()
                )
            }
            clean.contains("6º") || clean.contains("7º") || clean.contains("8º") || clean.contains("9º") -> {
                Pair(
                    "11 a 14 anos (Anos Finais do Ensino Fundamental)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (11 a 14 anos - Adolescentes):
                    - Linguagem juvenil, respeitosa e sem infantilização.
                    - Enunciados estruturados, sintetizados e diretos, focando na clareza conceitual.
                    """.trimIndent()
                )
            }
            clean.contains("médio") || clean.contains("medio") || clean.contains("eja") -> {
                Pair(
                    "15 a 17+ anos (Ensino Médio / EJA)",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA (15 a 17+ anos - Jovens e Adultos):
                    - Linguagem madura, contextualizada com trabalho, cidadania e vida prática, mantendo a síntese e clareza DUA.
                    """.trimIndent()
                )
            }
            else -> {
                Pair(
                    "Idade escolar correspondente ao $grade",
                    """
                    DIRETRIZ ETÁRIA MANDATÓRIA:
                    - Ajuste rigorosamente a extensão das frases e o vocabulário para a idade e maturidade do estudante do $grade, mantendo tom acolhedor e acessibilidade universal (DUA).
                    """.trimIndent()
                )
            }
        }
    }

    suspend fun generateQuestions(
        subject: String,
        grade: String,
        count: Int,
        type: String, // "MULTIPLE_CHOICE", "TRUE_FALSE", "DISCURSIVE", "ANY"
        profile: String = "REGULAR",
        nivelAutonomia: String = ""
    ): List<AIQuestao> = withContext(Dispatchers.IO) {
        val (ageDescriptor, ageGuideline) = getAgeRangeAndGuidelinesForGrade(grade)

        val typeConstraint = when (type) {
            "MULTIPLE_CHOICE" -> "Apenas questões de múltipla escolha (com alternativas A, B, C, D)."
            "TRUE_FALSE" -> "Apenas questões do tipo verdadeiro ou falso (V ou F) ou Sim/Não dependendo da faixa etária."
            "DISCURSIVE" -> "Apenas questões discursivas/abertas."
            "PALAVRAS_CRUZADAS" -> "Crie atividades com palavras cruzadas / cruzadinhas simples (preencha opcaoA, opcaoB, opcaoC, opcaoD com 'Palavra: Dica' da cruzadinha)."
            "CACA_PALAVRAS" -> "Crie atividades com caça-palavras simples (liste as palavras a encontrar nas opções ou no enunciado)."
            else -> "Questões mistas e lúdicas, podendo ser múltipla escolha, verdadeiro ou falso, ligar colunas, caça-palavras simples ou cruzadinhas acessíveis."
        }

        val autonomyInstruction = when {
            nivelAutonomia.contains("Não alfabetizado", ignoreCase = true) -> """
                DIRETRIZ DE AUTONOMIA NA LEITURA (ALUNO NÃO ALFABETIZADO - APOIO VISUAL TOTAL):
                - O aluno apresenta incapacidade de leitura convencional.
                - As questões DEVEM ser extremamente simples, objetivas e de fácil entendimento.
                - REGRA MANDATÓRIA DE OPÇÕES E ALTERNATIVAS VISUAIS: As opções para MARCAR (múltipla escolha) ou LIGAR/ASSOCIAR (MATCH) DEVEM SER PREFERENCIALMENTE CONSTRUÍDAS COM IMAGENS, EMOTICONS OU CARDS CAA AO LADO DO NOME (ex: '🍎 Maçã | 🍌 Banana', 'Sim 😊 | Não 🙁', '☀️ Sol | 🌧️ Chuva').
                - Substitua textos longos por comandos diretos e visuais (ex: 'Ligue', 'Pinte', 'Conte').
                - As alternativas devem ser curtas (1 palavra, símbolo ou emoji).
            """.trimIndent()
            nivelAutonomia.contains("processo de alfabetização", ignoreCase = true) -> """
                DIRETRIZ DE AUTONOMIA NA LEITURA (ALUNO EM PROCESSO DE ALFABETIZAÇÃO):
                - O aluno reconhece letras e sílabas iniciais, mas necessita de apoio pedagógico e frases curtas.
                - Enunciados com frases diretas e simples (máximo 1 a 2 frases).
                - REGRA MANDATÓRIA DE OPÇÕES E ALTERNATIVAS VISUAIS: As alternativas para MARCAR ou LIGAR DEVEM SER PREFERENCIALMENTE ACOMPANHADAS DE IMAGENS, EMOTICONS OU SÍMBOLOS CAA (ex: '🐶 Cachorro', '🚗 Carro') para permitir o reconhecimento visual e a associação imediata.
                - Apoio visual constante e vocabulário acessível do cotidiano.
            """.trimIndent()
            nivelAutonomia.contains("Independente", ignoreCase = true) || nivelAutonomia.contains("Alfabetizado", ignoreCase = true) -> """
                DIRETRIZ DE AUTONOMIA NA LEITURA (INDEPENDENTE / ALFABETIZADO):
                - Lê e escreve funcionalmente; utilize enunciados diretos, claros e objetivos.
            """.trimIndent()
            else -> ""
        }

        val profileInstruction = when (profile) {
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "TDAH", "SUPORTE_COGNITIVO", "SINDROME_DOWN" -> """
                Você é um especialista em Educação Inclusiva e Neurodiversidade. Sua tarefa é adaptar/gerar a avaliação para uma criança com Deficiência Intelectual (DI) severa, TDAH, Síndrome de Down, Autismo (nível de suporte elevado/severo) ou Apoio Cognitivo Extenso.

                Diretrizes obrigatórias de acessibilidade e adaptação cognitiva:
                1. TRANSPOSIÇÃO DE COMPLEXIDADE CONCEITUAL (MANDATÓRIO): Quando o tema ou os assuntos propostos forem de maior complexidade conceitual ou abstração (ex: ciclo da água, fotossíntese, operações matemáticas abstratas, história, ciências, fisiologia), estes assuntos DEVEM SER OBRIGATORIAMENTE APRESENTADOS EM NÍVEL DE BAIXA COMPLEXIDADE CONCRETA E PRÁTICA DO COTIDIANO DO ALUNO (ex: 'A planta precisa de sol ☀️ e água 💧 para crescer', '1 + 1 = 2 🍬🍬'), de modo a possibilitar a compreensão efetiva sem perder o tema indicado pelo professor.
                2. Elimine textos de contexto longos ou desnecessários. Enunciados devem ter no máximo 1 a 2 frases curtas, diretas e objetivas.
                3. Não utilize questões dissertativas/abertas longas. Substitua por marcar X, circular, ligar ou completar.
                4. Reduza as alternativas de múltipla escolha para 2 ou 3 opções bem distintas (preencha opcaoA, opcaoB e opcionalmente opcaoC; deixe opcaoD vazia "").
                5. Para alunos não alfabetizados ou em alfabetização, construa as alternativas para marcar ou ligar PREFERENCIALMENTE COM IMAGENS, EMOTICONS OU CARDS CAA (ex: 🍎, 🐶, 😊/🙁).
                6. REGRA RIGOROSA DE CARDS CAA / PICTOGRAMAS: No campo pictogramasSuporte, insira EXCLUSIVAMENTE substantivos concretos de objetos ou entidades físicas reais do tema (ex: 'MURO DE BERLIM', 'OTAN', 'BANDEIRA', 'SOL', 'PLANTA', 'CARRO'). É ESTRITAMENTE PROIBIDO incluir datas isoladas (ex: '1989'), verbos, frases abstratas longas ou termos matemáticos descontextualizados (JAMAIS use 'SINAL DE DIVISÃO' ou símbolos de aritmética em provas de História ou Português).
                7. Use fonte limpa e vocabulário simples, direto, afetuoso e sem frases ambíguas.
            """.trimIndent()
            "DISLEXIA" -> """
                Adapte esta questão sob os princípios do Design Universal para a Aprendizagem (DUA) para suporte leitor e fonológico (Dislexia):
                1. Frases na ordem direta (Sujeito + Verbo + Predicado) com vocabulário de fácil processamento.
                2. Evite construções ambíguas ou palavras com semelhança gráfica complexa.
            """.trimIndent()
            "BAIXA_VISAO", "ACESSIBILIDADE_VISUAL" -> """
                Adapte esta questão sob os princípios do Design Universal para a Aprendizagem (DUA) para Acessibilidade Visual:
                1. Descrições ricas, texturas, referências táteis e auditivas detalhadas no enunciado.
            """.trimIndent()
            "SURDEZ", "ACESSIBILIDADE_LINGUISTICA" -> """
                Adapte esta questão sob os princípios do Design Universal para a Aprendizagem (DUA) para Acessibilidade Comunicacional:
                1. Forte suporte pictórico, vocabulário visual e estruturação sintática objetiva.
            """.trimIndent()
            "SUPORTE_MULTISSENSORIAL" -> """
                Adapte esta questão sob os princípios do Design Universal para a Aprendizagem (DUA) para Suporte Multissensorial:
                1. Integração multissensorial, comandos acolhedores e diretos.
            """.trimIndent()
            "ALTAS_HABILIDADES" -> """
                Adapte esta questão para enriquecimento curricular e desafio cognitivo (Altas Habilidades / Superdotação):
                1. Pensamento crítico, conexões interdisciplinares e autonomia investigativa.
            """.trimIndent()
            else -> "Questão estruturada nos princípios do Design Universal para a Aprendizagem (DUA), garantindo acessibilidade para todos os estudantes."
        }

        val prompt = """
            IDENTIDADE E PAPEL
            Você é o Provalino, sistema automatizado especialista em elaboração de avaliações escolares pedagógicas para o Ensino Fundamental e Educação Infantil no Brasil. Você opera com base nas diretrizes do CFP, IFG, Educação Inclusiva e Design Universal para a Aprendizagem (DUA).
            Você é um agente de execução automática e não conversacional. Não faça perguntas nem produza texto livre. Receba os parâmetros, processe e entregue exclusivamente o JSON de saída.

            MODO DE OPERAÇÃO
            Parâmetros recebidos: $count (quantidade de questões), $subject (tema ou disciplina), $grade (ano escolar), $typeConstraint (tipo de questão), $profileInstruction (instruções DUA) e $autonomyInstruction (nível de leitura).
            Nunca emita texto livre fora do JSON.

            TAREFA PRINCIPAL
            Gere exatamente $count questões pedagógicas sobre o tema $subject, adequadas ao ano escolar $grade ($ageDescriptor), no tipo $typeConstraint, respeitando integralmente as diretrizes DUA, faixa etária e de autonomia.
            FIDELIDADE TOTAL AO PROFESSOR: É OBRIGATÓRIO seguir estritamente o pedido exato do professor em $subject. Se houver mais de um tema especificado, distribua as questões de forma igualitária entre cada um deles.

            DIRETRIZ MANDATÓRIA DE FAIXA ETÁRIA E LINGUAGEM DO ALUNO ($ageDescriptor):
            $ageGuideline

            DIRETRIZES GERAIS DE LINGUAGEM ACESSÍVEL E ACOLHEDORA:
            - Adequação imediata da linguagem ao nível de desenvolvimento do aluno daquela faixa etária.
            - NUNCA use formulações frias, burocráticas ou estilo concurso ('Assinale a alternativa correta', 'Avalie a proposição abaixo', 'Identifique o item condizente').
            - O vocabulário deve ser acolhedor, positivo e natural para a criança.
            - Para Educação Infantil e 1º/2º ano: NUNCA use Verdadeiro/Falso com as siglas (V)/(F). Use perguntas com 'Sim 😊 / Não 🙁' ou carinhas/emojis.

            DIRETRIZES PEDAGÓGICAS ESPECIAIS PARA TIPOS DE QUESTÕES:

            1. QUESTÕES COM A DIRETIVA DE "LIGUE" / ASSOCIAÇÃO (PAREAMENTO EM DUAS COLUNAS):
               - O campo 'tipo' DEVE SER "MATCH".
               - O enunciado deve ser claro (ex: "Ligue cada tipo de energia ao seu uso correspondente:").
               - As opções DEVEM conter os pares correspondentes separados por barra vertical ' | ':
                 "opcaoA": "Energia Solar | Aquecer a água",
                 "opcaoB": "Energia Elétrica | Acender a lâmpada",
                 "opcaoC": "Energia Eólica | Mover as pás do moinho",
                 "opcaoD": ""
               - NÃO junte todas as opções em uma única linha com 'Coluna esquerda' e 'Coluna direita'. Coloque cada par correspondente em sua respectiva opção (opcaoA, opcaoB, opcaoC, etc.).
               - As opções serão desenhadas automaticamente pelo aplicativo como dois cards organizados em paralelo (lado a lado).

            2. QUESTÕES COM COMANDOS COMO "PINTE" / "COLORIR":
               - Enunciado adaptado para a atividade de colorir (ex: 'Pinte o contorno da figura e marque o nome correto:').
               - No campo da questão deve constar a indicação clara do contorno/desenho a ser colorido (ex: '[Desenho em contorno para colorir: Árvore]').
               - Se o pedido acompanhar solicitação para que o aluno preencha o nome da figura representada, marque este espaço logo abaixo com a linha: 'Nome da figura: _______________________'.
               - REGRA CRÍTICA E PROIBIÇÃO: NÃO use emojis, card CAA e/ou pictogramas coloridos como a imagem a ser pintada! Emojis e emoticons são TERMINANTEMENTE PROIBIDOS como ocupantes do papel de imagem a ser pintada (pois já vêm coloridos digitalmente e a criança precisa de contorno para pintar no papel).

            3. QUESTÕES DE MATEMÁTICA E OPERAÇÕES CONCRETAS:
               - Conforme o nível de suporte e nível de autonomia, utilize emojis para realizar as operações concretas marcando as quantidades para contagem direta.
               - Exemplo: se a questão pede para somar as balas de Pedro com as de Miguel, no enunciado/topo da resposta coloque os emojis enfileirados lado a lado: '🍬 🍬 🍬 🍬 🍬  +  🍬 🍬 🍬 = [   ]'.
               - Crianças com alto suporte tendem a não reconhecer números abstratos; a questão deve fornecer imagens para contagem direta.

            4. ALUNOS NÃO ALFABETIZADOS OU EM PROCESSO:
               - Questões simplificadas, sem blocos longos de leitura, com predominância de recursos visuais, pareamento e contagem direta.

            5. DIRETRIZES DE USO DE CARTÕES CAA E SUPORTE VISUAL (LÓGICA CAA PURA E CONCRETITUDE):
               - LÓGICA CAA MANDATÓRIA: Quando preencher 'pictogramasSuporte', forneça ESTRITAMENTE CONCEITOS-CHAVE E SUBSTANTIVOS CONCRETOS DA MATÉRIA EM MAIÚSCULAS SEPARADOS POR VÍRGULA (Exemplos: "MAÇÃ, SOMA", "ESTRELA, CONTAGEM", "GATO, ANIMAL", "ÁRVORE, PLANTA").
               - PROIBIÇÃO ABSOLUTA DE FRASES DE COMANDO: É TERMINANTEMENTE PROIBIDO colocar frases de instrução ou verbos de comando no 'pictogramasSuporte' (NUNCA coloque "SOME AS QUANTIDADES", "CONTE OS OBJETOS", "CALCULE A SOMA", "LEIA A FRASE", "MARQUE A RESPOSTA"). A Comunicação Alternativa (CAA) utiliza conceitos-chave concretos, nunca frases gramaticais completas do professor.
               - O campo 'pictogramasSuporte' DEVE ser preenchido APENAS quando estritamente indispensável para a aprendizagem (ex: TEA de alto suporte, Deficiência Intelectual). Na maioria das questões, DEVE SER DEIXADO VAZIO "".
               - PROIBIÇÃO DE REPLICAR ALTERNATIVAS OU VERDADEIRO/FALSO: NUNCA crie cartões CAA com 'Verdadeiro', 'Falso', 'V', 'F' ou copiando opções (A, B, C, D).
               - PROIBIÇÃO DE METATAGS OU TERMOS DE DIAGRAMAÇÃO: É TERMINANTEMENTE PROIBIDO incluir no 'pictogramasSuporte' termos como 'CARTÕES CAA', 'DUAS COLUNAS', 'COLUNAS', 'LIGUE', 'ESPAÇO PARA COLORIR', 'ESPAÇO PARA DESENHO' ou qualquer instrução gráfica. Insira EXCLUSIVAMENTE substantivos e conceitos reais e concretos (ex: BOLA, MAÇÃ, CARRO, ESCOLA).
               - PROIBIÇÃO DE EMOJIS GENÉRICOS DE PREENCHIMENTO: NUNCA insira emojis genéricos repetidos (como '📚 ✏️' ou '✨ 📖') apenas para preencher o campo. Se não houver substantivo/conceito concreto específico, deixe 'pictogramasSuporte': "".

            PROIBIÇÕES GERAIS E CONFERÊNCIA OBRIGATÓRIA:
            - É PROIBIDO criar questões com associações sem um sentido lógico claro dentro da proposta didática.
            - É PROIBIDO fugir da matéria ou assunto especificado.
            - É PROIBIDO ignorar qualquer detalhe da solicitação do professor.
            - SEMPRE confira se a questão atende a todas as regras antes de incluí-la.

            FORMATO DE SAÍDA OBRIGATÓRIO (APENAS JSON):
            {
              "questoes": [
                {
                  "enunciado": "Enunciado claro, contextualizado e adaptado.",
                  "tipo": "MULTIPLE_CHOICE",
                  "opcaoA": "Texto da alternativa A",
                  "opcaoB": "Texto da alternativa B",
                  "opcaoC": "Texto da alternativa C",
                  "opcaoD": "Texto da alternativa D",
                  "respostaCorreta": "A",
                  "assunto": "$subject",
                  "anoEscolar": "$grade",
                  "codigoBNCC": "EF05CI02",
                  "pictogramasSuporte": ""
                }
              ]
            }
        """.trimIndent()

        val systemInstructionText = "Você é o Provalino AI — assistente especialista em Provas Adaptadas com IA, inclusão pedagógica, DUA, AEE e diretrizes do MEC para Educação Infantil e Ensino Fundamental. DIRETRIZ MÁXIMA: 100% das questões devem ser exclusivamente sobre o assunto '$subject'. Proibido criar questões genéricas, fora do escopo ou sem sentido lógico pedagógico. É TERMINANTEMENTE PROIBIDO usar emojis coloridos como imagens a serem pintadas. Em matemática com suporte, forneça contagem visual concreta com emojis enfileirados. Proibido conteúdo discriminatório, violento ou sexual. O aplicativo é 100% laico, apartidário e protege as crianças conforme o ECA e a BNCC."

        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val apiKey = if (rawApiKey.isNotBlank() && rawApiKey != "MY_GEMINI_API_KEY") {
            rawApiKey
        } else {
            ""
        }

        if (apiKey.isNotEmpty()) {
            val modelsToTry = listOf("gemini-3.5-flash-lite", "gemini-3-flash-preview", "gemini-3.1-flash-lite")
            val request = GenerateContentRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.7
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstructionText))
                )
            )

            for (modelName in modelsToTry) {
                try {
                    Log.d("ProvalinoAI", "Tentando gerar com modelo Gemini: $modelName")
                    val response = service.generateContentWithModel(modelName, apiKey, request)
                    val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (rawText != null) {
                        val jsonText = cleanJsonString(rawText)
                        val adapter = moshi.adapter(AIQuestoesResponse::class.java)
                        val aiResponse = try {
                            adapter.fromJson(jsonText)
                        } catch (e: Exception) {
                            if (jsonText.startsWith("[")) {
                                val listType = Types.newParameterizedType(List::class.java, AIQuestao::class.java)
                                val listAdapter = moshi.adapter<List<AIQuestao>>(listType)
                                listAdapter.fromJson(jsonText)?.let { AIQuestoesResponse(it) }
                            } else null
                        }
                        val sanitized = validateAndSanitizeQuestions(aiResponse?.questoes, type, profile, subject)
                        if (sanitized.isNotEmpty()) return@withContext sanitized
                    }
                } catch (e: Exception) {
                    Log.e("ProvalinoAI", "Tentativa com Gemini modelo $modelName falhou: ${e.message}")
                }
            }
        } else {
            Log.w("ProvalinoAI", "Chave API do Gemini não configurada.")
        }

        // Fallback para Pollinations AI com modelo gpt-5.6-luna
        try {
            val pollinationRawText = callPollinationsFallback(systemInstructionText, prompt)
            if (pollinationRawText != null) {
                val jsonText = cleanJsonString(pollinationRawText)
                val adapter = moshi.adapter(AIQuestoesResponse::class.java)
                val aiResponse = try {
                    adapter.fromJson(jsonText)
                } catch (e: Exception) {
                    if (jsonText.startsWith("[")) {
                        val listType = Types.newParameterizedType(List::class.java, AIQuestao::class.java)
                        val listAdapter = moshi.adapter<List<AIQuestao>>(listType)
                        listAdapter.fromJson(jsonText)?.let { AIQuestoesResponse(it) }
                    } else null
                }
                val sanitized = validateAndSanitizeQuestions(aiResponse?.questoes, type, profile, subject)
                if (sanitized.isNotEmpty()) return@withContext sanitized
            }
        } catch (e: Exception) {
            Log.e("ProvalinoPollination", "Falha no fallback Pollinations AI para generateQuestions: ${e.message}", e)
        }

        // Se a API não retornou questões ou ocorreu falha de rede/serviço
        emptyList()
    }

    private fun generateLocalFallbackQuestions(
        subject: String,
        grade: String,
        count: Int,
        type: String,
        profile: String
    ): List<AIQuestao> {
        val list = mutableListOf<AIQuestao>()
        val emojiMap = mapOf(
            "TEA" to "🧩 📌",
            "TDAH" to "⚡ 🎯",
            "DISLEXIA" to "📖 ✨",
            "SUPORTE_COGNITIVO" to "🌸 🔢",
            "ACESSIBILIDADE_VISUAL" to "👁️ 🧠",
            "ACESSIBILIDADE_LINGUISTICA" to "👂 💬",
            "SUPORTE_MULTISSENSORIAL" to "🤝 🌟",
            "ALTAS_HABILIDADES" to "🚀 💡",
            "REGULAR" to "📚 ✏️"
        )
        val supportEmoji = emojiMap[profile] ?: "📚 ✏️"

        val isCiencias = subject.contains("Ciên", true) || subject.contains("Matéria", true) || subject.contains("Energia", true)
        val isMatematica = subject.contains("Mat", true)
        val isPortugues = subject.contains("Port", true) || subject.contains("Ling", true)
        val isHistoria = subject.contains("Hist", true)
        val isGeografia = subject.contains("Geog", true)

        val isFundamental1 = grade.contains("1º") || grade.contains("2º") || grade.contains("3º") || grade.contains("4º") || grade.contains("5º") || grade.contains("Infantil", true)

        for (i in 1..count) {
            val qType = if (type == "ANY") {
                if (i % 3 == 0) "TRUE_FALSE" else if (i % 3 == 1) "MULTIPLE_CHOICE" else "DISCURSIVE"
            } else type

            val modIndex = (i - 1) % 5

            val questao = when {
                isPortugues -> {
                    if (isFundamental1) {
                        when (modIndex) {
                            0 -> AIQuestao(
                                enunciado = "Em uma leitura atenta, identifique a palavra que funciona como substantivo comum:",
                                tipo = qType,
                                opcaoA = "Borboleta 🦋",
                                opcaoB = "Alegremente",
                                opcaoC = "Rapidamente",
                                opcaoD = "Cantarolando",
                                respostaCorreta = "A",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF03LP08",
                                pictogramasSuporte = supportEmoji
                            )
                            1 -> AIQuestao(
                                enunciado = "Assinale a alternativa em que todas as palavras apresentam separação silábica correta:",
                                tipo = qType,
                                opcaoA = "Es-co-la / A-lu-no / Li-vro 📚",
                                opcaoB = "Esc-ola / Al-uno / Liv-ro",
                                opcaoC = "E-scola / A-lun-o / Li-vr-o",
                                opcaoD = "Es-col-a / Alun-o / L-ivro",
                                respostaCorreta = "A",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF02LP04",
                                pictogramasSuporte = supportEmoji
                            )
                            2 -> AIQuestao(
                                enunciado = "Avalie a afirmação sobre pontuação na frase simples:\n\nAfirmação: O ponto final (.) deve ser utilizado ao término de frases declarativas.",
                                tipo = "TRUE_FALSE",
                                opcaoA = "",
                                opcaoB = "",
                                opcaoC = "",
                                opcaoD = "",
                                respostaCorreta = "V",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF03LP01",
                                pictogramasSuporte = supportEmoji
                            )
                            3 -> AIQuestao(
                                enunciado = "No trecho 'O cãozinho latia com alegria no jardim', qual é o núcleo do sujeito da oração?",
                                tipo = qType,
                                opcaoA = "Cãozinho 🐶",
                                opcaoB = "Alegria",
                                opcaoC = "Jardim",
                                opcaoD = "Latia",
                                respostaCorreta = "A",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF05LP04",
                                pictogramasSuporte = supportEmoji
                            )
                            else -> AIQuestao(
                                enunciado = "Observe a imagem mental da história. Escreva um parágrafo curto narrando a atitude inclusiva dos colegas na brincadeira do recreio.",
                                tipo = "DISCURSIVE",
                                opcaoA = "",
                                opcaoB = "",
                                opcaoC = "",
                                opcaoD = "",
                                respostaCorreta = "Resposta narrativa textual sobre cooperação e acolhimento.",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF04LP15",
                                pictogramasSuporte = supportEmoji
                            )
                        }
                    } else {
                        // 6º ao 9º Ano
                        when (modIndex) {
                            0 -> AIQuestao(
                                enunciado = "Na frase 'Os estudantes da turma adaptada apresentaram o projeto com maestria', identifique o sujeito da oração:",
                                tipo = qType,
                                opcaoA = "Os estudantes da turma adaptada ✍️",
                                opcaoB = "Apresentaram o projeto",
                                opcaoC = "Com maestria",
                                opcaoD = "Apenas 'projeto'",
                                respostaCorreta = "A",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF07LP05",
                                pictogramasSuporte = supportEmoji
                            )
                            1 -> AIQuestao(
                                enunciado = "Assinale a alternativa que indica o sentido correto da figura de linguagem (metáfora) na oração 'A leitura é uma janela para o mundo':",
                                tipo = qType,
                                opcaoA = "Significa que a leitura amplia o conhecimento e o pensamento crítico. 📖",
                                opcaoB = "Significa que a leitura exige abrir uma janela física de vidro.",
                                opcaoC = "Significa que o livro é transparente como o vidro.",
                                opcaoD = "Significa que a frase não tem nenhum sentido figurado.",
                                respostaCorreta = "A",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF08LP03",
                                pictogramasSuporte = supportEmoji
                            )
                            2 -> AIQuestao(
                                enunciado = "Avalie a afirmação gramatical sobre regência e coesão textual:\n\nAfirmação: Os pronomes relativos garantem a coesão semântica ao evitar repetições desnecessárias no texto.",
                                tipo = "TRUE_FALSE",
                                opcaoA = "",
                                opcaoB = "",
                                opcaoC = "",
                                opcaoD = "",
                                respostaCorreta = "V",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF09LP08",
                                pictogramasSuporte = supportEmoji
                            )
                            3 -> AIQuestao(
                                enunciado = "Qual das opções apresenta um exemplo de conjunção adversativa, que expressa uma ideia de oposição?",
                                tipo = qType,
                                opcaoA = "Porém, contudo, todavia 🔄",
                                opcaoB = "Porque, visto que, já que",
                                opcaoC = "E, nem, tampouco",
                                opcaoD = "Portanto, logo, por conseguinte",
                                respostaCorreta = "A",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF07LP12",
                                pictogramasSuporte = supportEmoji
                            )
                            else -> AIQuestao(
                                enunciado = "Redija um texto dissertativo-argumentativo curto (5 a 8 linhas) sobre a importância da empatia e da diversidade no ambiente escolar.",
                                tipo = "DISCURSIVE",
                                opcaoA = "",
                                opcaoB = "",
                                opcaoC = "",
                                opcaoD = "",
                                respostaCorreta = "Texto dissertativo abordando respeito à diversidade, empatia e inclusão social.",
                                assunto = subject,
                                anoEscolar = grade,
                                codigoBNCC = "EF08LP14",
                                pictogramasSuporte = supportEmoji
                            )
                        }
                    }
                }
                isMatematica -> {
                    when (modIndex) {
                        0 -> AIQuestao(
                            enunciado = "Resolva o seguinte problema prático para o $grade: Se uma turma tem 24 alunos e cada um recebeu 3 cadernos, quantos cadernos foram distribuídos no total?",
                            tipo = qType,
                            opcaoA = "72 cadernos 📚",
                            opcaoB = "27 cadernos",
                            opcaoC = "64 cadernos",
                            opcaoD = "80 cadernos",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05MA08",
                            pictogramasSuporte = supportEmoji
                        )
                        1 -> AIQuestao(
                            enunciado = "Qual é o valor numérico do perímetro de um quadrado cujo lado mede 6 centímetros?",
                            tipo = qType,
                            opcaoA = "24 cm 📐",
                            opcaoB = "36 cm",
                            opcaoC = "12 cm",
                            opcaoD = "18 cm",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF04MA20",
                            pictogramasSuporte = supportEmoji
                        )
                        2 -> AIQuestao(
                            enunciado = "Avalie a afirmativa matemática a seguir:\n\nAfirmação: A fração 2/4 representa exatamente a mesma quantidade que 1/2 (frações equivalentes).",
                            tipo = "TRUE_FALSE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "V",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05MA05",
                            pictogramasSuporte = supportEmoji
                        )
                        3 -> AIQuestao(
                            enunciado = "Um comerciante vendeu uma mercadoria por R$ 150,00 com um desconto de 10%. Quanto o cliente pagou?",
                            tipo = qType,
                            opcaoA = "R$ 135,00 💵",
                            opcaoB = "R$ 140,00",
                            opcaoC = "R$ 125,00",
                            opcaoD = "R$ 130,00",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF07MA02",
                            pictogramasSuporte = supportEmoji
                        )
                        else -> AIQuestao(
                            enunciado = "Explique com suas palavras como podemos utilizar a regra de três simples para calcular proporções no dia a dia.",
                            tipo = "DISCURSIVE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "Explicação da relação direta/inversa entre duas grandezas proporcionais.",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF08MA13",
                            pictogramasSuporte = supportEmoji
                        )
                    }
                }
                isCiencias -> {
                    when (modIndex) {
                        0 -> AIQuestao(
                            enunciado = "O que acontece com a água líquida quando é colocada no congelador por várias horas?",
                            tipo = qType,
                            opcaoA = "Transforma-se em gelo (estado sólido) 🧊",
                            opcaoB = "Transforma-se em vapor de água",
                            opcaoC = "Desaparece completamente",
                            opcaoD = "Mantém-se exatamente igual",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05CI01",
                            pictogramasSuporte = supportEmoji
                        )
                        1 -> AIQuestao(
                            enunciado = "Qual das seguintes fontes de energia é considerada renovável e limpa para o meio ambiente?",
                            tipo = qType,
                            opcaoA = "Energia solar (luz do sol) ☀️",
                            opcaoB = "Queima de carvão mineral",
                            opcaoC = "Derivados de petróleo",
                            opcaoD = "Gás natural fóssil",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05CI02",
                            pictogramasSuporte = supportEmoji
                        )
                        2 -> AIQuestao(
                            enunciado = "Avalie a afirmativa científica a seguir:\n\nAfirmação: As plantas realizam fotossíntese utilizando luz solar, água e gás carbônico para produzir seu próprio alimento.",
                            tipo = "TRUE_FALSE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "V",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF04CI02",
                            pictogramasSuporte = supportEmoji
                        )
                        3 -> AIQuestao(
                            enunciado = "Qual órgão do sistema circulatório humano é responsável por bombear o sangue para todo o corpo?",
                            tipo = qType,
                            opcaoA = "Coração 🫀",
                            opcaoB = "Pulmão",
                            opcaoC = "Estômago",
                            opcaoD = "Fígado",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05CI06",
                            pictogramasSuporte = supportEmoji
                        )
                        else -> AIQuestao(
                            enunciado = "Descreva a importância da reciclagem do lixo e do consumo consciente para a preservação dos ecossistemas.",
                            tipo = "DISCURSIVE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "Redução do impacto ambiental, reaproveitamento de materiais e proteção da fauna e flora.",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05CI05",
                            pictogramasSuporte = supportEmoji
                        )
                    }
                }
                isHistoria -> {
                    when (modIndex) {
                        0 -> AIQuestao(
                            enunciado = "O que estuda a História e qual a importância de conhecermos os registros do passado pessoal e familiar?",
                            tipo = qType,
                            opcaoA = "Compreender nossa origem, cultura e a evolução da sociedade 🏛️",
                            opcaoB = "Apenas decorar datas antigas sem significado",
                            opcaoC = "Prever exclusivamente o clima do dia seguinte",
                            opcaoD = "Calcular operações matemáticas complexas",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF04HI01",
                            pictogramasSuporte = supportEmoji
                        )
                        1 -> AIQuestao(
                            enunciado = "Quais são as principais fontes históricas utilizadas pelos historiadores para reconstruir os fatos do passado?",
                            tipo = qType,
                            opcaoA = "Documentos escritos, fotografias, objetos antigos e relatos orais 📜",
                            opcaoB = "Apenas conversas informais sem registros",
                            opcaoC = "Fórmulas científicas de laboratório",
                            opcaoD = "Desenhos animados modernos",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF04HI03",
                            pictogramasSuporte = supportEmoji
                        )
                        2 -> AIQuestao(
                            enunciado = "Avalie a afirmativa histórica a seguir:\n\nAfirmação: Os povos indígenas já habitavam o território brasileiro muito antes da chegada dos colonizadores europeus em 1500.",
                            tipo = "TRUE_FALSE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "V",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05HI02",
                            pictogramasSuporte = supportEmoji
                        )
                        3 -> AIQuestao(
                            enunciado = "Qual foi o principal motivo histórico que impulsionou o processo de urbanização no Brasil durante o século XX?",
                            tipo = qType,
                            opcaoA = "O crescimento das indústrias e a busca por oportunidades de trabalho nas cidades 🏭",
                            opcaoB = "A proibição do uso de terras agrícolas",
                            opcaoC = "A redução total da população rural",
                            opcaoD = "O fim da energia elétrica nas áreas rurais",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF08HI16",
                            pictogramasSuporte = supportEmoji
                        )
                        else -> AIQuestao(
                            enunciado = "Comente sobre a Declaração Universal dos Direitos Humanos e o papel da cidadania na construção de uma sociedade justa e sem preconceitos.",
                            tipo = "DISCURSIVE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "A cidadania garante direitos fundamentais e o dever de respeitar as diferenças entre todos os seres humanos.",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF09HI09",
                            pictogramasSuporte = supportEmoji
                        )
                    }
                }
                isGeografia -> {
                    when (modIndex) {
                        0 -> AIQuestao(
                            enunciado = "Qual a principal diferença entre o espaço rural (campo) e o espaço urbano (cidade)?",
                            tipo = qType,
                            opcaoA = "O campo destaca-se pela agricultura e natureza, enquanto a cidade concentra comércio e indústrias 🌾🏙️",
                            opcaoB = "Ambos possuem exatamente a mesma quantidade de edifícios altos",
                            opcaoC = "No campo não existem seres vivos",
                            opcaoD = "A cidade é desprovida de habitantes",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF04GE01",
                            pictogramasSuporte = supportEmoji
                        )
                        1 -> AIQuestao(
                            enunciado = "Qual dos seguintes elementos caracteriza uma paisagem modificada pela ação humana (paisagem cultural)?",
                            tipo = qType,
                            opcaoA = "Rodovias asfaltadas, pontes e edifícios residenciais 🛣️",
                            opcaoB = "Florestas nativas intocadas",
                            opcaoC = "Montanhas rochosas naturais",
                            opcaoD = "Rios correndo livremente no vale profundo",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF04GE04",
                            pictogramasSuporte = supportEmoji
                        )
                        2 -> AIQuestao(
                            enunciado = "Avalie a afirmativa geográfica a seguir:\n\nAfirmação: Os mapas são representações gráficas em escala reduzida da superfície terrestre ou de parte dela.",
                            tipo = "TRUE_FALSE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "V",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF06GE08",
                            pictogramasSuporte = supportEmoji
                        )
                        3 -> AIQuestao(
                            enunciado = "O que são recursos naturais renováveis?",
                            tipo = qType,
                            opcaoA = "Recursos que se regeneram naturalmente em um curto período, como vento, luz solar e água 💨☀️",
                            opcaoB = "Recursos que nunca mais se renovam após o primeiro uso",
                            opcaoC = "Minérios de ferro e petróleo bruto extraídos da terra",
                            opcaoD = "Produtos sintéticos criados exclusivamente em laboratórios",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF07GE06",
                            pictogramasSuporte = supportEmoji
                        )
                        else -> AIQuestao(
                            enunciado = "Explique a importância da preservação das bacias hidrográficas para o abastecimento sustentável de água nas grandes cidades.",
                            tipo = "DISCURSIVE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "Proteção das nascentes, controle da poluição dos rios e garantia de água potável para a população.",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF08GE15",
                            pictogramasSuporte = supportEmoji
                        )
                    }
                }
                else -> {
                    when (modIndex) {
                        0 -> AIQuestao(
                            enunciado = "Qual é a principal finalidade das regras de convivência em um ambiente escolar e comunitário?",
                            tipo = qType,
                            opcaoA = "Garantir o respeito mútuo, a ordem e o bem-estar de todos 🤝",
                            opcaoB = "Impedir qualquer forma de diálogo entre as pessoas",
                            opcaoC = "Tornar as atividades mais lentas e difíceis",
                            opcaoD = "Servir apenas para enfeitar as paredes",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05ER01",
                            pictogramasSuporte = supportEmoji
                        )
                        1 -> AIQuestao(
                            enunciado = "Por que a preservação do meio ambiente e o uso consciente da água são vitais para a sociedade?",
                            tipo = qType,
                            opcaoA = "Para assegurar recursos essenciais e a vida saudável para as presentes e futuras gerações 🌱",
                            opcaoB = "Porque os recursos naturais são inesgotáveis e nunca acabam",
                            opcaoC = "Para evitar que as pessoas tomem banho",
                            opcaoD = "Não há importância alguma na preservação ambiental",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05CI05",
                            pictogramasSuporte = supportEmoji
                        )
                        2 -> AIQuestao(
                            enunciado = "Avalie a afirmativa a seguir:\n\nAfirmação: Ouvir o colega com atenção e empatia demonstra respeito e favorece um clima escolar acolhedor.",
                            tipo = "TRUE_FALSE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "V",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF05EF03",
                            pictogramasSuporte = supportEmoji
                        )
                        3 -> AIQuestao(
                            enunciado = "Qual atitude promove a inclusão social no cotidiano escolar?",
                            tipo = qType,
                            opcaoA = "Acolher a todos com igualdade de oportunidades e valorizar suas singularidades 🌟",
                            opcaoB = "Isolar colegas em atividades individuais",
                            opcaoC = "Excluir alunos que possuem diferentes ritmos de aprendizagem",
                            opcaoD = "Ignorar as necessidades de suporte dos colegas",
                            respostaCorreta = "A",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF06ER07",
                            pictogramasSuporte = supportEmoji
                        )
                        else -> AIQuestao(
                            enunciado = "Escreva uma mensagem de incentivo destacando como a cooperação entre a família e a escola fortalece o desenvolvimento dos estudantes.",
                            tipo = "DISCURSIVE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = "Texto ressaltando o trabalho em parceria entre escola e família.",
                            assunto = subject,
                            anoEscolar = grade,
                            codigoBNCC = "EF07ER04",
                            pictogramasSuporte = supportEmoji
                        )
                    }
                }
            }

            val finalQuestao = if (profile == "SUPORTE_COGNITIVO") {
                questao.copy(opcaoC = "", opcaoD = "")
            } else {
                questao
            }

            list.add(finalQuestao)
        }
        return list
    }

    suspend fun adaptExistingQuestion(
        enunciado: String,
        tipo: String,
        opcaoA: String,
        opcaoB: String,
        opcaoC: String,
        opcaoD: String,
        profile: String
    ): AIQuestao? = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val apiKey = if (rawApiKey.isNotBlank() && rawApiKey != "MY_GEMINI_API_KEY") {
            rawApiKey
        } else {
            ""
        }
        val profileDirectives = when (profile) {
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "TDAH", "SUPORTE_COGNITIVO", "SINDROME_DOWN" -> """
                Você é um especialista em Educação Inclusiva e Neurodiversidade. Sua tarefa é adaptar a avaliação fornecida para uma criança com Deficiência Intelectual (DI) severa, TDAH, Síndrome de Down, Autismo (nível de suporte elevado/severo) ou Apoio Cognitivo Extenso.

                Diretrizes obrigatórias de acessibilidade:
                1. TRANSPOSIÇÃO DE COMPLEXIDADE (REGRA MANDATÓRIA): Se o enunciado ou o tema abordar conteúdos de alta complexidade ou abstração, transponha o conceito para uma apresentação de BAIXA COMPLEXIDADE CONCRETA E PRÁTICA DO COTIDIANO (ex: 'A planta precisa de sol ☀️ e água 💧', '1 + 1 = 2 🍬🍬').
                2. Elimine textos de contexto longos ou desnecessários. Enunciados devem ter no máximo 1 a 2 frases curtas, diretas e objetivas.
                3. Não utilize questões dissertativas/abertas. Substitua por marcar X, circular, ligar ou completar com palavras de um banco de opções.
                4. Reduza as alternativas de múltipla escolha para apenas 2 ou 3 opções bem distintas (preencha opcaoA, opcaoB e opcionalmente opcaoC; deixe opcaoD vazia "").
                5. Para alunos em alfabetização ou não alfabetizados, construa/adapte as alternativas para marcar ou ligar PREFERENCIALMENTE COM IMAGENS, EMOTICONS OU CARDS CAA (ex: 🍎, 🐶, 😊/🙁).
                6. Inclua indicações claras de onde deve haver suporte visual e pictogramas universais de CAA para crianças autistas (ex: [Pictograma/Imagem: Carro 🚗]).
                7. Use fonte limpa e vocabulário simples, direto, afetuoso e literal.
                Faça os ajustes redacionais para manter a coesão.
            """.trimIndent()
            else -> "Adapte com base no Design Universal para a Aprendizagem (DUA) para o perfil $profile."
        }

        val prompt = """
            $profileDirectives

            Questão original para adaptação:
            Enunciado: $enunciado
            Tipo: $tipo
            Alternativa A: $opcaoA
            Alternativa B: $opcaoB
            Alternativa C: $opcaoC
            Alternativa D: $opcaoD

            Gere exclusivamente um JSON válido com os campos: enunciado, tipo, opcaoA, opcaoB, opcaoC, opcaoD, respostaCorreta, assunto, anoEscolar, codigoBNCC, pictogramasSuporte.
        """.trimIndent()
        val systemPrompt = "Você é o Provalino AI, especialista em Educação Inclusiva, DUA, CAA e AEE. Retorne estritamente em JSON."

        if (apiKey.isNotEmpty()) {
            val modelsToTry = listOf("gemini-3.5-flash-lite", "gemini-3-flash-preview", "gemini-3.1-flash-lite")
            val request = GenerateContentRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.5
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )

            for (modelName in modelsToTry) {
                try {
                    val response = service.generateContentWithModel(modelName, apiKey, request)
                    val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (rawText != null) {
                        val jsonText = cleanJsonString(rawText)
                        val adapter = moshi.adapter(AIQuestoesResponse::class.java)
                        val aiResponse = try {
                            adapter.fromJson(jsonText)
                        } catch (e: Exception) {
                            if (jsonText.startsWith("[")) {
                                val listType = Types.newParameterizedType(List::class.java, AIQuestao::class.java)
                                val listAdapter = moshi.adapter<List<AIQuestao>>(listType)
                                listAdapter.fromJson(jsonText)?.let { AIQuestoesResponse(it) }
                            } else null
                        }
                        val sanitized = validateAndSanitizeQuestions(aiResponse?.questoes, tipo, profile)
                        if (sanitized.isNotEmpty()) return@withContext sanitized.first()
                    }
                } catch (e: Exception) {
                    Log.w("ProvalinoAI", "Tentativa com modelo $modelName em adaptExistingQuestion falhou: ${e.message}")
                }
            }
        }

        // Fallback para Pollinations AI com modelo gpt-5.6-luna
        try {
            val pollinationRawText = callPollinationsFallback(systemPrompt, prompt)
            if (pollinationRawText != null) {
                val jsonText = cleanJsonString(pollinationRawText)
                val adapter = moshi.adapter(AIQuestoesResponse::class.java)
                val aiResponse = try {
                    adapter.fromJson(jsonText)
                } catch (e: Exception) {
                    if (jsonText.startsWith("[")) {
                        val listType = Types.newParameterizedType(List::class.java, AIQuestao::class.java)
                        val listAdapter = moshi.adapter<List<AIQuestao>>(listType)
                        listAdapter.fromJson(jsonText)?.let { AIQuestoesResponse(it) }
                    } else null
                }
                val sanitized = validateAndSanitizeQuestions(aiResponse?.questoes, tipo, profile)
                if (sanitized.isNotEmpty()) return@withContext sanitized.first()
            }
        } catch (e: Exception) {
            Log.e("ProvalinoPollination", "Falha no fallback Pollinations AI para adaptExistingQuestion: ${e.message}", e)
        }

        // Fallback local adaptado para garantir sucesso absoluto
        val prefix = when(profile) {
            "TEA" -> "🧩 [Adaptado TEA - Linguagem Literal]: "
            "TDAH" -> "⚡ [Adaptado TDAH - Foco Direto]: "
            "DISLEXIA" -> "📖 [Adaptado Dislexia - Frases Curtas]: "
            "SUPORTE_COGNITIVO" -> "🌸 [Apoio Cognitivo & Concreto]: "
            "ACESSIBILIDADE_VISUAL" -> "👁️ [Acessibilidade Visual]: "
            "ACESSIBILIDADE_LINGUISTICA" -> "👂 [Acessibilidade Comunicacional]: "
            "SUPORTE_MULTISSENSORIAL" -> "🤝 [Suporte Multissensorial]: "
            "ALTAS_HABILIDADES" -> "🚀 [Enriquecimento Curricular]: "
            else -> "✨ [Adaptado DUA]: "
        }
        AIQuestao(
            enunciado = prefix + enunciado,
            tipo = tipo,
            opcaoA = opcaoA,
            opcaoB =opcaoB,
            opcaoC = if (profile == "SUPORTE_COGNITIVO") "" else opcaoC,
            opcaoD = if (profile == "SUPORTE_COGNITIVO") "" else opcaoD,
            respostaCorreta = "A",
            assunto = "Adaptação Pedagógica",
            anoEscolar = "Ensino Fundamental",
            codigoBNCC = "EF01CI01",
            pictogramasSuporte = ""
        )
    }

    private fun cleanJsonString(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json", ignoreCase = true)) {
            cleaned = cleaned.substring(7)
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3)
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length - 3)
        }
        cleaned = cleaned.trim()

        val firstBrace = cleaned.indexOf('{')
        val firstBracket = cleaned.indexOf('[')
        val startIdx = when {
            firstBrace != -1 && firstBracket != -1 -> minOf(firstBrace, firstBracket)
            firstBrace != -1 -> firstBrace
            firstBracket != -1 -> firstBracket
            else -> -1
        }

        val lastBrace = cleaned.lastIndexOf('}')
        val lastBracket = cleaned.lastIndexOf(']')
        val endIdx = maxOf(lastBrace, lastBracket)

        if (startIdx != -1 && endIdx != -1 && endIdx >= startIdx) {
            cleaned = cleaned.substring(startIdx, endIdx + 1)
        }

        return cleaned.trim()
    }

    private fun stripOptionPrefix(text: String, prefixLetter: String): String {
        var cleaned = text.trim()
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

    private fun validateAndSanitizeQuestions(
        questions: List<AIQuestao>?,
        requestedType: String,
        requestedProfile: String,
        requestedSubject: String = ""
    ): List<AIQuestao> {
        if (questions.isNullOrEmpty()) return emptyList()

        val result = mutableListOf<AIQuestao>()
        for (q in questions) {
            var rawEnunciado = q.enunciado.trim()
                .replace("[Questão Discursiva - Escreva sua resposta dissertativa abaixo]", "")
                .replace("(Atenção: responda de forma direta e literal)", "")
                .replace("(Atenção: responda de forma direta e literal).", "")
                .trim()

            if (rawEnunciado.isBlank() || rawEnunciado.length < 5) continue
            if (rawEnunciado.contains("Atividade adaptada", ignoreCase = true) && rawEnunciado.length < 20) continue

            var normalizedType = q.tipo.uppercase().trim()
            if (ActivityGridHelper.isCrosswordQuestion(normalizedType, rawEnunciado)) {
                normalizedType = "PALAVRAS_CRUZADAS"
            } else if (ActivityGridHelper.isWordSearchQuestion(normalizedType, rawEnunciado)) {
                normalizedType = "CACA_PALAVRAS"
            } else if (MatchQuestionHelper.isMatchQuestion(normalizedType, rawEnunciado, q.opcaoA, q.opcaoB)) {
                normalizedType = "MATCH"
            } else if (normalizedType !in listOf("MULTIPLE_CHOICE", "TRUE_FALSE", "DISCURSIVE", "MATCH", "PALAVRAS_CRUZADAS", "CACA_PALAVRAS")) {
                normalizedType = when {
                    requestedType != "ANY" && requestedType.isNotBlank() -> requestedType
                    q.opcaoA.isNotBlank() && q.opcaoB.isNotBlank() -> "MULTIPLE_CHOICE"
                    rawEnunciado.contains("Verdadeiro", ignoreCase = true) || rawEnunciado.contains("Falso", ignoreCase = true) -> "TRUE_FALSE"
                    else -> "DISCURSIVE"
                }
            }

            val finalSubject = if (q.assunto.isBlank()) requestedSubject else q.assunto.trim()

            when (normalizedType) {
                "PALAVRAS_CRUZADAS", "CACA_PALAVRAS" -> {
                    result.add(
                        q.copy(
                            enunciado = rawEnunciado,
                            tipo = normalizedType,
                            opcaoA = q.opcaoA.trim(),
                            opcaoB = q.opcaoB.trim(),
                            opcaoC = q.opcaoC.trim(),
                            opcaoD = q.opcaoD.trim(),
                            respostaCorreta = q.respostaCorreta.trim(),
                            assunto = finalSubject,
                            anoEscolar = q.anoEscolar.trim(),
                            codigoBNCC = q.codigoBNCC.trim(),
                            pictogramasSuporte = PictogramInjector.extractTerms(q.pictogramasSuporte).joinToString(", ")
                        )
                    )
                }
                "MATCH" -> {
                    result.add(
                        q.copy(
                            enunciado = rawEnunciado,
                            tipo = "MATCH",
                            opcaoA = q.opcaoA.trim(),
                            opcaoB = q.opcaoB.trim(),
                            opcaoC = q.opcaoC.trim(),
                            opcaoD = q.opcaoD.trim(),
                            respostaCorreta = q.respostaCorreta.trim(),
                            assunto = finalSubject,
                            anoEscolar = q.anoEscolar.trim(),
                            codigoBNCC = q.codigoBNCC.trim(),
                            pictogramasSuporte = PictogramInjector.extractTerms(q.pictogramasSuporte).joinToString(", ")
                        )
                    )
                }
                "MULTIPLE_CHOICE" -> {
                    val opA = stripOptionPrefix(q.opcaoA, "A")
                    val opB = stripOptionPrefix(q.opcaoB, "B")
                    if (opA.isBlank() || opB.isBlank()) continue

                    val isHighSupportProfile = requestedProfile in listOf("TEA", "AUTISMO", "DEF_INTELECTUAL", "TDAH", "SUPORTE_COGNITIVO", "SINDROME_DOWN")
                    val opC = stripOptionPrefix(q.opcaoC, "C")
                    val opD = if (isHighSupportProfile && opC.isBlank()) "" else if (isHighSupportProfile) "" else stripOptionPrefix(q.opcaoD, "D")

                    var resp = q.respostaCorreta.uppercase().trim()
                    if (resp !in listOf("A", "B", "C", "D")) resp = "A"

                    result.add(
                        q.copy(
                            enunciado = rawEnunciado,
                            tipo = "MULTIPLE_CHOICE",
                            opcaoA = opA,
                            opcaoB = opB,
                            opcaoC = opC,
                            opcaoD = opD,
                            respostaCorreta = resp,
                            assunto = finalSubject,
                            anoEscolar = q.anoEscolar.trim(),
                            codigoBNCC = q.codigoBNCC.trim(),
                            pictogramasSuporte = PictogramInjector.extractTerms(q.pictogramasSuporte).joinToString(", ")
                        )
                    )
                }
                "TRUE_FALSE" -> {
                    val cleanAno = q.anoEscolar.lowercase()
                    val isEarlyAge = cleanAno.contains("1º") || cleanAno.contains("1o") ||
                                    cleanAno.contains("2º") || cleanAno.contains("2o") ||
                                    cleanAno.contains("infantil") || cleanAno.contains("creche") ||
                                    cleanAno.contains("pré") || cleanAno.contains("pre")

                    val optALabel = if (isEarlyAge) "Sim 😊" else "Verdadeiro (V)"
                    val optBLabel = if (isEarlyAge) "Não 🙁" else "Falso (F)"

                    var resp = q.respostaCorreta.uppercase().trim()
                    resp = when {
                        resp in listOf("V", "TRUE", "VERDADEIRO", "A", "SIM", "CERTO") -> "A"
                        resp in listOf("F", "FALSE", "FALSO", "B", "NAO", "NÃO", "ERRADO") -> "B"
                        else -> "A"
                    }

                    val sanitizedEnunciado = rawEnunciado
                        .replace(Regex("""\(\s*\)\s*Verdadeiro\s*\(V\)\s*\(\s*\)\s*Falso\s*\(F\)""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""\(\s*\)\s*Sim\s*\(\s*\)\s*Não""", RegexOption.IGNORE_CASE), "")
                        .trim()

                    result.add(
                        q.copy(
                            enunciado = sanitizedEnunciado,
                            tipo = "TRUE_FALSE",
                            opcaoA = optALabel,
                            opcaoB = optBLabel,
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = resp,
                            assunto = finalSubject,
                            anoEscolar = q.anoEscolar.trim(),
                            codigoBNCC = q.codigoBNCC.trim(),
                            pictogramasSuporte = PictogramInjector.extractTerms(q.pictogramasSuporte).joinToString(", ")
                        )
                    )
                }
                "DISCURSIVE" -> {
                    result.add(
                        q.copy(
                            enunciado = rawEnunciado,
                            tipo = "DISCURSIVE",
                            opcaoA = "",
                            opcaoB = "",
                            opcaoC = "",
                            opcaoD = "",
                            respostaCorreta = q.respostaCorreta.trim(),
                            assunto = finalSubject,
                            anoEscolar = q.anoEscolar.trim(),
                            codigoBNCC = q.codigoBNCC.trim(),
                            pictogramasSuporte = PictogramInjector.extractTerms(q.pictogramasSuporte).joinToString(", ")
                        )
                    )
                }
            }
        }
        // Aplica o enriquecimento determinístico e ARASAAC via PictogramInjector
        return result.map { PictogramInjector.enrichAIQuestaoPictograms(it, requestedProfile) }
    }
}
