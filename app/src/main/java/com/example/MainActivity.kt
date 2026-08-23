package com.example

import com.aistudio.provalino.teacher.abcxyz.R
import com.example.ui.LoginScreen
import com.example.ui.PictogramSupportRow
import com.example.ui.components.ProvalinoEmptyState
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Surface
import com.example.data.AppUpdateState
import com.example.data.PictogramInjector
import com.example.data.PictogramCatalog
import com.example.data.AlunoNecessidadeEspecial
import com.example.data.PdfShareHelper
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider


import android.os.Bundle
import android.webkit.WebView
import android.print.PrintManager
import android.print.PrintAttributes
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayColors
import com.example.ui.theme.ClayTabButton
import com.example.ui.theme.ClayCoinBadge
import com.example.ui.theme.ClayTextField
import com.example.ui.theme.ClayLoadingSpinner
import com.example.ui.theme.ClayLoadingDots
import com.example.ui.theme.ClayLoadingCard
import com.example.ui.theme.clayBounce
import com.example.ui.theme.clayScale
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NotaAluno
import com.example.data.Prova
import com.example.data.Questao
import com.example.data.Turma
import com.example.ui.ProvalinoViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ProvalinoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvalinoApp(viewModel: ProvalinoViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val turmas by viewModel.turmas.collectAsState()
    val questoes by viewModel.questoes.collectAsState()
    val provas by viewModel.provas.collectAsState()
    val alunosInclusao by viewModel.alunosInclusao.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val activeProvaForGrades by viewModel.activeProvaForGrades.collectAsState()
    val gradesForActiveProva by viewModel.gradesForActiveProva.collectAsState()
    val moedas by viewModel.moedas.collectAsState()
    val showAdDialog by viewModel.showAdDialog.collectAsState()
    val adType by viewModel.adType.collectAsState()
    val adLimitMessage by viewModel.adLimitMessage.collectAsState()
    val showTour by viewModel.showTour.collectAsState()
    val tourStep by viewModel.tourStep.collectAsState()
    val updateState by viewModel.appUpdateState.collectAsState()
    val offlineNoQuestionsState by viewModel.offlineNoQuestionsState.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    val isAdmin = currentUser?.email == "marcio.moura2708@gmail.com" || currentUser?.email == "admteste@example.com"

    // Primeiro acesso: Exibe o tour guiado do Provalino automaticamente
    LaunchedEffect(currentUser?.uid) {
        val userUid = currentUser?.uid
        if (userUid != null) {
            val prefs = context.getSharedPreferences("provalino_prefs", android.content.Context.MODE_PRIVATE)
            val hasSeenTour = prefs.getBoolean("has_seen_tour_$userUid", false)
            if (!hasSeenTour) {
                prefs.edit().putBoolean("has_seen_tour_$userUid", true).apply()
                viewModel.startTour()
            }
        }
    }

    if (showPrivacyPolicyDialog) {
        com.example.ui.PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    if (updateState.isUpdateAvailable) {
        UpdateAppDialog(
            updateState = updateState,
            onDismiss = { viewModel.dismissUpdateDialog() },
            onUpdateClick = {
                val urlToOpen = if (updateState.updateUrl.isNotBlank()) updateState.updateUrl else "https://play.google.com/store/apps/details?id=${updateState.playStorePackage}"
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${updateState.playStorePackage}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(marketIntent)
                    } catch (e2: Exception) {
                        // Ignora falha de abertura externa silenciosamente
                    }
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // --- LINHA 1 (PRINCIPAL): Nome do App com Ícone do Mascote ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentScreen != "home") {
                                IconButton(
                                    onClick = { viewModel.setScreen("home") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Voltar para o Início",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Image(
                                painter = painterResource(id = R.drawable.img_provalino_blue_owl_avatar_1787513526538),
                                contentDescription = "Mascote Provalino",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = "Provalino AI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // --- LINHA 2 (SECUNDÁRIA - FONTE MENOR): Moedas/Professora à esquerda e Ações (Cadeado, i, X) à direita ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Identificação do Professor e Botão de Moedas em Clay
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ClayCoinBadge(
                                coins = moedas,
                                onClick = { viewModel.openAdModal("REWARDED") }
                            )

                            Text(
                                text = "Prof(a). ${currentUser?.email?.substringBefore("@") ?: "Docente"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Ações secundárias: Cadeado (Privacidade), i (Tour), X (Sair)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = { showPrivacyPolicyDialog = true },
                                modifier = Modifier.size(32.dp).clayBounce()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Política de Privacidade",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.startTour() },
                                modifier = Modifier.size(32.dp).clayBounce()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Guia e Tour do Aplicativo",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.signOut() },
                                modifier = Modifier.size(32.dp).clayBounce()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Sair da conta",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFAFAFA),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { viewModel.setScreen("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == "inclusao",
                    onClick = { viewModel.setScreen("inclusao") },
                    icon = { Text("🧑‍🎓", fontSize = 18.sp) },
                    label = { Text("Alunos", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_alunos")
                )

                NavigationBarItem(
                    selected = currentScreen == "provas",
                    onClick = { viewModel.setScreen("provas") },
                    icon = { Text("📝", fontSize = 18.sp) },
                    label = { Text("Provas", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_provas")
                )

                if (isAdmin) {
                    NavigationBarItem(
                        selected = currentScreen == "developer",
                        onClick = { viewModel.setScreen("developer") },
                        icon = { Text("⚙️", fontSize = 18.sp) },
                        label = { Text("Dev / Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_dev")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFFFFFFF))
                    )
                )
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    alunoCount = alunosInclusao.size,
                    questaoCount = questoes.size,
                    provaCount = provas.size
                )
                "inclusao" -> CarteirasScreen(
                    viewModel = viewModel,
                    alunos = alunosInclusao
                )
                "questoes" -> QuestoesScreen(
                    viewModel = viewModel,
                    questoes = questoes,
                    isGenerating = isGenerating,
                    aiError = aiError,
                    onClearError = { viewModel.clearAIError() }
                )
                "provas" -> ProvasScreen(
                    viewModel = viewModel,
                    provas = provas,
                    turmas = turmas,
                    alunos = alunosInclusao,
                    onVerDetalhes = { prova ->
                        viewModel.selectProvaForGrades(prova)
                        viewModel.setScreen("detalhes_prova")
                    }
                )
                "developer" -> if (isAdmin) {
                    com.example.ui.DevDashboardScreen(
                        viewModel = viewModel,
                        currentUserEmail = currentUser?.email ?: ""
                    )
                } else {
                    viewModel.setScreen("home")
                }
                "nova_prova" -> NovaProvaScreen(
                    viewModel = viewModel,
                    turmas = turmas,
                    questoes = questoes
                )
                "detalhes_prova" -> activeProvaForGrades?.let { prova ->
                    DetalhesProvaScreen(
                        viewModel = viewModel,
                        prova = prova,
                        turmas = turmas,
                        grades = gradesForActiveProva,
                        onVoltar = { viewModel.setScreen("provas") }
                    )
                } ?: viewModel.setScreen("provas")
            }
        }

        if (isGenerating) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1E88E5),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "✨ Provalino AI Criando Atividade...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0D47A1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Processando diretrizes DUA/AEE, alinhando à BNCC e estruturando a avaliação pronta para impressão...",
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF1E88E5),
                            trackColor = Color(0xFFE3F2FD)
                        )
                    }
                }
            }
        }

        if (offlineNoQuestionsState != null) {
            val state = offlineNoQuestionsState!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissOfflineDialogAndRefund() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📡 ", fontSize = 22.sp)
                        Text("Sem Conexão / Questões Não Encontradas", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Não encontramos questões salvas no banco de dados local para o assunto:\n\"${state.subject}\".",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = "É necessária uma conexão ativa com a internet para que o Provalino AI crie esta prova com o assunto solicitado.",
                            fontSize = 13.sp,
                            color = Color(0xFF333333)
                        )
                        val apiKeyConfigured = com.aistudio.provalino.teacher.abcxyz.BuildConfig.GEMINI_API_KEY.isNotEmpty() && com.aistudio.provalino.teacher.abcxyz.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
                        if (!apiKeyConfigured) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "🔑 Nota: A chave 'GEMINI_API_KEY' precisa ser informada no painel de Segredos (Secrets) do AI Studio para que as requisições à IA sejam concluídas.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (!state.isRetry)
                                    "🪙 Se você desistir, suas 2 moedas serão reembolsadas imediatamente. Você também pode conectar à internet e tentar novamente sem custo adicional."
                                else
                                    "🔄 Tentativa realizada sem custo adicional de moedas.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.retryOfflineGeneration() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("🔄 Tentar Novamente", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.dismissOfflineDialogAndRefund() }
                    ) {
                        Text("❌ Desistir e Reembolsar", color = Color(0xFFD32F2F))
                    }
                }
            )
        }

        if (showAdDialog) {
            SimulatedAdDialog(
                adType = adType,
                adLimitMessage = adLimitMessage,
                onClose = { granted -> viewModel.closeAdModal(granted) }
            )
        }

        if (aiError != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearAIError() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📡  ", fontSize = 22.sp)
                        Text("Erro de Conexão / API", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = aiError ?: "É necessária uma conexão ativa com a internet para comunicar com o Provalino AI e construir a prova.",
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "🪙 Reembolso Efetuado: Suas 2 moedas foram devolvidas à sua conta automaticamente!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearAIError() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("OK, Entendido", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }



        if (showTour) {
            val steps = listOf(
                Triple(
                    "🦉 Bem-vindo ao Provalino AI!",
                    "Seu assistente inteligente para criar avaliações diferenciadas, inclusivas e personalizadas para seus alunos com facilidade e rapidez.",
                    "Passo 1 de 4"
                ),
                Triple(
                    "🧩 1. Carteira de Alunos Inclusivos",
                    "Cadastre seus alunos com necessidades especiais na carteira de inclusão. O Provalino usa esses dados para gerar avaliações perfeitamente adaptadas e prontas para cada perfil.",
                    "Passo 2 de 4"
                ),
                Triple(
                    "✨ 2. Geração Inteligente de Provas",
                    "Use o fluxo de sala de aula e clique no botão de gerar atividade para criar avaliações pedagógicas completas com IA. A IA ajusta automaticamente o nível de complexidade e inclusão!",
                    "Passo 3 de 4"
                ),
                Triple(
                    "📚 3. Histórico e Economia ('Provas')",
                    "Todas as provas geradas ficam salvas automaticamente no banco de dados local. Você pode visualizá-las e reutilizá-las na aba 'Provas' a qualquer momento sem gastar moedas novas!",
                    "Passo 4 de 4"
                )
            )
            val currentStepInfo = steps.getOrElse(tourStep) { steps[0] }

            AlertDialog(
                onDismissRequest = { viewModel.dismissTour() },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = currentStepInfo.third,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentStepInfo.first,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = currentStepInfo.second,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 19.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 4) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (i == tourStep) 10.dp else 6.dp)
                                        .background(
                                            color = if (i == tourStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.dismissTour() },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Pular", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tourStep > 0) {
                                OutlinedButton(
                                    onClick = { viewModel.prevTourStep() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Anterior", fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = { viewModel.nextTourStep() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (tourStep == 3) "Concluir 🎉" else "Próximo ➜",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

// --- MASCOTE INTERATIVO ANIMADO REAGINDO AO PROGRESSO ---

@Composable
fun ProvalinoMascotProgressWidget(
    completedCount: Int,
    totalGoal: Int = 10,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "owl_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "owl_y_float"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "owl_scale"
    )

    val progressRatio = (completedCount.toFloat() / totalGoal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    // Estimativa de tempo economizado: ~15 minutos por questão adaptada manualmente
    val minutesSaved = completedCount * 15
    val hoursSavedText = if (minutesSaved >= 60) {
        val h = minutesSaved / 60
        val m = minutesSaved % 60
        if (m == 0) "${h}h" else "${h}h ${m}min"
    } else {
        "${minutesSaved} min"
    }

    val message = when {
        completedCount == 0 -> "👋 Olá, Professor(a)! Crie sua primeira prova em segundos e ganhe até 3 horas livres por semana!"
        completedCount in 1..3 -> "⚡ Você já economizou ~$hoursSavedText de trabalho manual! Adeus noites formatando avaliações!"
        completedCount in 4..9 -> "🔥 Incrível! ~$hoursSavedText poupados com IA! Mais tempo para você e inclusão real para seus alunos."
        else -> "🏆 Sensacional! Você já economizou mais de $hoursSavedText de planejamento com o Provalino! Sua rotina docente agradece! 🦉✨"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Owl Mascot Animated Box in Claymation
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            translationY = offsetY
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(CircleShape)
                        .background(ClayColors.TealLight.copy(alpha = 0.5f))
                        .border(2.5.dp, ClayColors.Teal, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_provalino_blue_owl_avatar_1787513526538),
                        contentDescription = "Coruja Mestre Provalino em Massinha",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Speech Bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    ),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "🦉 Coruja Provalino reage:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Progress Bar Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progresso de Atividades",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedCount de $totalGoal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

// --- 1. HOME SCREEN ---

@Composable
fun HomeScreen(
    viewModel: ProvalinoViewModel,
    alunoCount: Int,
    questaoCount: Int,
    provaCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Banner Card in Claymation style
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White,
                borderColor = ClayColors.TealLight,
                elevation = 4.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_provalino_hero_banner_pt_1787513538843),
                    contentDescription = "Coruja Provalino AI Banner em Massinha",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Widget do Mascote Coruja com Animação Reativa de Progresso
        item {
            ProvalinoMascotProgressWidget(
                completedCount = questaoCount,
                totalGoal = 10
            )
        }

        // Banner Interativo de Ajuda / Tour do Provalino
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.startTour() }
                    .testTag("home_card_tour_ajuda"),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = ClayColors.TealLight.copy(alpha = 0.25f),
                borderColor = ClayColors.TealLight,
                elevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(ClayColors.Teal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 22.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Não sabe por onde começar?",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = ClayColors.TealDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Clique aqui, o Provalino te ajuda! 🦉✨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayColors.Teal
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Iniciar Tour",
                        tint = ClayColors.Teal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Summary Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = "🧑‍🎓",
                    count = alunoCount,
                    title = "Alunos",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "❓",
                    count = questaoCount,
                    title = "Questões IA",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "📝",
                    count = provaCount,
                    title = "Provas",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Big Main Action Buttons
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color.White,
                borderColor = ClayColors.TealLight,
                elevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ações Rápidas",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = ClayColors.TextPrimary
                    )

                    ClayButton(
                        onClick = { viewModel.setScreen("questoes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("home_btn_gerar_ia"),
                        backgroundColor = ClayColors.Purple,
                        darkShadowColor = ClayColors.PurpleDark,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🤖  ", fontSize = 18.sp)
                            Text("Gerar Atividades com Provalino AI", fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    ClayButton(
                        onClick = { viewModel.setScreen("inclusao") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("home_btn_alunos"),
                        backgroundColor = ClayColors.Blue,
                        darkShadowColor = ClayColors.BlueDark,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧑‍🎓  ", fontSize = 18.sp)
                            Text("Alunos na Sala ($alunoCount/9)", fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    ClayButton(
                        onClick = { viewModel.setScreen("nova_prova") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("home_btn_criar_prova"),
                        backgroundColor = ClayColors.Green,
                        darkShadowColor = ClayColors.GreenDark,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝  ", fontSize = 18.sp)
                            Text("Montar Prova Adaptada", fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Quick Tips Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                    Column {
                        Text(
                            text = "Como funciona o Provalino AI?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Cadastre até 9 carteiras de alunos nas necessidades especiais (TEA, TDAH, Dislexia, etc) e grau de severidade.\n2. Gere atividades e questões 100% adaptadas com Provalino AI.\n3. Monte provas pedagógicas inclusivas para Educação Infantil e Ensino Fundamental seguindo as normas DUA/AEE do MEC!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Ad Banner Component for Google AdMob & Monetization
        item {
            AdBannerView(
                title = "Apoie a Educação Inclusiva",
                description = "Parcerias de anúncios e Google AdMob garantem o Provalino AI 100% gratuito para professores."
            )
        }
    }
}

@Composable
fun StatCard(icon: String, count: Int, title: String, modifier: Modifier = Modifier) {
    ClayCard(
        modifier = modifier.clayBounce(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color.White,
        borderColor = ClayColors.TealLight,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = ClayColors.TealDark
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = ClayColors.TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- ADS BANNER VIEW COMPONENT ---

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3904073010190363/1234567890",
    title: String = "Provalino AI - Parceiro AdMob",
    description: String = "Anúncios e parcerias mantêm o aplicativo gratuito e acessível para todos os professores."
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ad_banner_container"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFC107), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ANÚNCIO / AD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Espaço do Patrocinador",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "Google AdMob",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE2E8F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📢", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- PROFILE SELECTOR ROW ---

@Composable
fun ProfileSelectorRow(selected: String, onSelected: (String) -> Unit) {
    val profiles = listOf(
        Triple("REGULAR", "Padrão", "Gera uma questão adaptada padrão para Educação Infantil e Ensino Fundamental."),
        Triple("TEA", "Autismo (TEA) 🧩", "Linguagem direta, literal, rotina visual com pictogramas universais/emojis e suporte de previsão."),
        Triple("SINDROME_DOWN", "Síndrome de Down 💛", "Enunciados curtos, pictogramas visuais universais e no máximo 2 a 3 alternativas."),
        Triple("DEF_INTELECTUAL", "Def. Intelectual 🧠", "Conceitos concretos, apoio visual reforçado e foco em questões objetivas."),
        Triple("TDAH", "TDAH ⚡", "Comandos em DESTAQUE, tópicos curtos e estimulação focada sem textos longos."),
        Triple("DISLEXIA", "Dislexia 📖", "Frases na ordem direta, vocabulário simplificado e alto contraste."),
        Triple("SUPORTE_COGNITIVO", "Apoio Cognitivo 🌸", "Conceitos concretos, apoio visual e no máximo 3 alternativas claras (DUA)."),
        Triple("ACESSIBILIDADE_VISUAL", "Acess. Visual 👁️", "Descrições táteis e auditivas ricas para leitor de tela ou ampliação."),
        Triple("ACESSIBILIDADE_LINGUISTICA", "Acess. Linguística 👂", "Vocabulário visual, imagens e estruturação sintática objetiva."),
        Triple("SUPORTE_MULTISSENSORIAL", "Multissensorial 🤝", "Multi-sensorialidade, comandos curtos e máximo nível de acolhimento."),
        Triple("ALTAS_HABILIDADES", "Altas Habilidades 🚀", "Desafios cognitivos, pensamento crítico e autonomia.")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Perfil de Adaptação Inclusiva (DUA/AEE):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E88E5))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profiles.forEach { (code, label, _) ->
                val isSelected = selected == code
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color(0xFF1E88E5) else Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelected(code) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        val currentDesc = profiles.firstOrNull { it.first == selected }?.third ?: ""
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F8E9), RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, Color(0xFFDCEDC8)), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "💡 Provalino AI explica: $currentDesc",
                fontSize = 11.sp,
                color = Color(0xFF33691E),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- QUESTOES SCREEN ---

@Composable
fun QuestoesScreen(
    viewModel: ProvalinoViewModel,
    questoes: List<Questao>,
    isGenerating: Boolean,
    aiError: String?,
    onClearError: () -> Unit
) {
    val context = LocalContext.current

    // Bank View State
    var selectedFilterPerfil by remember { mutableStateOf("TODOS") }
    var showAdaptDialog by remember { mutableStateOf(false) }
    var questionToAdapt by remember { mutableStateOf<Questao?>(null) }
    var targetAdaptPerfil by remember { mutableStateOf("TEA") }

    val filteredQuestoes = if (selectedFilterPerfil == "TODOS") {
        questoes
    } else {
        questoes.filter { it.perfilAdaptacao == selectedFilterPerfil }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mascot Owl Progress Widget on Activities screen
            item {
                ProvalinoMascotProgressWidget(
                    completedCount = questoes.size,
                    totalGoal = 10
                )
            }

            // --- BANCO DE QUESTOES ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Filtrar por Perfil de Inclusão:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF333333))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val filterOptions = listOf(
                                "TODOS" to "Mostrar Tudo 📚",
                                "REGULAR" to "Regular 📝",
                                "TEA" to "Autismo 🧩",
                                "TDAH" to "TDAH ⚡",
                                "DISLEXIA" to "Dislexia 📖",
                                "SUPORTE_COGNITIVO" to "Apoio Cognitivo 🌸"
                            )
                            filterOptions.forEach { (code, label) ->
                                val isSelected = selectedFilterPerfil == code
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelected) Color(0xFF0288D1) else Color(0xFFE1F5FE),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedFilterPerfil = code }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color(0xFF01579B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredQuestoes.isEmpty()) {
                item {
                    ProvalinoEmptyState(
                        title = "Banco de Questões Vazio",
                        description = "Nenhuma questão foi encontrada para este filtro. Tente alterar o filtro de perfil ou gerar novas questões de inclusão!",
                        buttonText = "⚡ Ir para Início",
                        onButtonClick = { viewModel.setScreen("home") }
                    )
                }
            } else {
                items(filteredQuestoes) { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = when (q.perfilAdaptacao) {
                                "TEA" -> Color(0xFF90CAF9)
                                "TDAH" -> Color(0xFFFFE082)
                                "DISLEXIA" -> Color(0xFFCE93D8)
                                "SUPORTE_COGNITIVO" -> Color(0xFFFFAB91)
                                "ACESSIBILIDADE_VISUAL" -> Color(0xFFD7CCC8)
                                "ACESSIBILIDADE_LINGUISTICA" -> Color(0xFFB2DFDB)
                                "SUPORTE_MULTISSENSORIAL" -> Color(0xFFF8BBD0)
                                "ALTAS_HABILIDADES" -> Color(0xFFB9F6CA)
                                else -> Color(0xFFE0E0E0)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (badgeText, badgeBg, badgeFg) = when (q.perfilAdaptacao) {
                                        "TEA" -> Triple("🧩 Autismo (TEA)", Color(0xFFE3F2FD), Color(0xFF0D47A1))
                                        "TDAH" -> Triple("⚡ TDAH", Color(0xFFFFF8E1), Color(0xFFF57F17))
                                        "DISLEXIA" -> Triple("📖 Dislexia", Color(0xFFF3E5F5), Color(0xFF4A148C))
                                        "SUPORTE_COGNITIVO" -> Triple("🌸 Apoio Cognitivo", Color(0xFFFBE9E7), Color(0xFFD84315))
                                        "ACESSIBILIDADE_VISUAL" -> Triple("👁️ Acess. Visual", Color(0xFFEFEBE9), Color(0xFF4E342E))
                                        "ACESSIBILIDADE_LINGUISTICA" -> Triple("👂 Acess. Linguística", Color(0xFFE0F7FA), Color(0xFF006064))
                                        "SUPORTE_MULTISSENSORIAL" -> Triple("🤝 Multissensorial", Color(0xFFFCE4EC), Color(0xFF880E4F))
                                        "ALTAS_HABILIDADES" -> Triple("🚀 Altas Habilidades", Color(0xFFE0F2F1), Color(0xFF00695C))
                                        else -> Triple("📝 Regular/Padrão", Color(0xFFECEFF1), Color(0xFF37474F))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(badgeText, fontSize = 11.sp, color = badgeFg, fontWeight = FontWeight.Bold)
                                    }

                                    val typeLabel = when (q.tipo) {
                                        "MULTIPLE_CHOICE" -> "A/B/C/D"
                                        "TRUE_FALSE" -> "V ou F"
                                        else -> "Aberta"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEDE7F6), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(typeLabel, fontSize = 10.sp, color = Color(0xFF5E35B1), fontWeight = FontWeight.Medium)
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteQuestao(q.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir questão",
                                        tint = Color(0xFFE53935)
                                    )
                                }
                            }

                            if (q.codigoBNCC.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8EAF6), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("📌 BNCC: ${q.codigoBNCC}", fontSize = 10.sp, color = Color(0xFF283593), fontWeight = FontWeight.Bold)
                                }
                            }

                            if (q.pictogramasSuporte.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                PictogramSupportRow(rawPictogramText = q.pictogramasSuporte)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = q.enunciado,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            if (q.tipo == "MULTIPLE_CHOICE") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (q.opcaoA.isNotBlank()) Text("A) ${formatOptionText(q.opcaoA, "A")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                    if (q.opcaoB.isNotBlank()) Text("B) ${formatOptionText(q.opcaoB, "B")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                    if (q.opcaoC.isNotBlank()) Text("C) ${formatOptionText(q.opcaoC, "C")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                    if (q.opcaoD.isNotBlank()) Text("D) ${formatOptionText(q.opcaoD, "D")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                }
                            } else if (q.tipo == "TRUE_FALSE") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("• (   ) Verdadeiro (V)", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                    Text("• (   ) Falso (F)", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Resposta: ${q.respostaCorreta}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                Button(
                                    onClick = {
                                        questionToAdapt = q
                                        showAdaptDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪄 ", fontSize = 11.sp)
                                        Text("Adaptar IA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Ad Banner Component
            item {
                AdBannerView(
                    title = "Atividades IA & Anúncios Parceiros",
                    description = "Espaço reservado para banners publicitários do AdMob durante a criação de questões."
                )
            }
        }
    }

    // --- DIALOG DE ADAPTAÇÃO COM IA ---
    if (showAdaptDialog && questionToAdapt != null) {
        val q = questionToAdapt!!
        AlertDialog(
            onDismissRequest = { if (!isGenerating) showAdaptDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪄 Adaptar com Lino IA", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF9C27B0))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Você está adaptando uma questão já existente no seu banco.", fontSize = 13.sp, color = Color(0xFF555555))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Questão Original:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF777777))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (q.enunciado.length > 80) q.enunciado.take(80) + "..." else q.enunciado,
                                fontSize = 12.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    Text("Escolha o perfil inclusivo de destino:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    val adaptOptions = listOf(
                        "TEA" to "Autismo (TEA) 🧩",
                        "TDAH" to "TDAH ⚡",
                        "DISLEXIA" to "Dislexia 📖",
                        "SUPORTE_COGNITIVO" to "Apoio Cognitivo 🌸"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        adaptOptions.forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (!isGenerating) targetAdaptPerfil = code }
                                    .background(
                                        color = if (targetAdaptPerfil == code) Color(0xFFF3E5F5) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = targetAdaptPerfil == code,
                                    onClick = { if (!isGenerating) targetAdaptPerfil = code }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val detailTip = when (targetAdaptPerfil) {
                        "TEA" -> "Provalino AI irá reescrever a questão sem figuras de linguagem ou duplo sentido, e adicionará rotina visual com emojis."
                        "TDAH" -> "Provalino AI colocará instruções de comando em NEGRITO MAIÚSCULO e simplificará o texto em itens para maior foco visual."
                        "DISLEXIA" -> "Provalino AI reescreverá em ordem direta e vocabulário amigável para facilitação de decodificação fonológica."
                        else -> "Provalino AI simplificará as ideias, usará contexto prático do dia a dia e adaptará para o formato inclusivo."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = "💡 $detailTip", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                    }

                    if (isGenerating) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ClayLoadingDots(
                                dotSize = 8.dp,
                                colors = listOf(ClayColors.Teal, Color(0xFF9C27B0), ClayColors.Orange)
                            )
                            Text("O Provalino AI está reescrevendo a questão... Aguarde...", fontSize = 12.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adaptQuestionWithAI(q.id, targetAdaptPerfil) { success ->
                            if (success) {
                                Toast.makeText(context, "Questão adaptada com sucesso e adicionada ao banco!", Toast.LENGTH_SHORT).show()
                                showAdaptDialog = false
                            } else {
                                Toast.makeText(context, "Falha ao adaptar questão.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Text("🪄 Adaptar com Provalino AI (2 🪙)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAdaptDialog = false },
                    enabled = !isGenerating
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
@Composable
fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFF9C27B0) else Color(0xFFF3E5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF9C27B0),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- 4. PROVAS SCREEN ---

@Composable
fun ProvasScreen(
    viewModel: ProvalinoViewModel,
    provas: List<Prova>,
    turmas: List<Turma>,
    alunos: List<AlunoNecessidadeEspecial> = emptyList(),
    onVerDetalhes: (Prova) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("TODAS") } // "TODAS", "ALUNO", "MATERIA"
    var showCreateProvaDialog by remember { mutableStateOf(false) }

    // Dialog state for modern AI Exam Generator
    var generationMode by remember { mutableStateOf(if (alunos.isNotEmpty()) "ALUNO" else "GERAL") } // "ALUNO" or "GERAL"
    var selectedAluno by remember { mutableStateOf<AlunoNecessidadeEspecial?>(alunos.firstOrNull()) }
    var selectedSerie by remember { mutableStateOf("3º Ano Fundamental") }
    var selectedPerfil by remember { mutableStateOf("REGULAR") }
    var selectedMateria by remember { mutableStateOf("Língua Portuguesa") }
    var customAssunto by remember { mutableStateOf("") }
    var qtdQuestoes by remember { mutableStateOf("4") }
    var customTitulo by remember { mutableStateOf("") }
    var expandedSerie by remember { mutableStateOf(false) }

    val filteredProvas = provas.filter { p ->
        val query = searchQuery.lowercase().trim()
        val matchesQuery = query.isEmpty() ||
                p.titulo.lowercase().contains(query) ||
                p.descricao.lowercase().contains(query)

        val matchesFilter = when (filterCategory) {
            "ALUNO" -> p.titulo.contains("Aluno:", ignoreCase = true) || p.descricao.contains("Aluno:", ignoreCase = true) || p.titulo.contains("Adaptada:", ignoreCase = true)
            "MATERIA" -> p.descricao.contains("Matéria:", ignoreCase = true) || p.titulo.contains("-", ignoreCase = true) || p.turmaId != null
            else -> true
        }

        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "📝 Minhas Provas Geradas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Provas completas adaptadas por aluno e matéria",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Button(
                    onClick = { showCreateProvaDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_criar_prova_header")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Prova", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("✨ Gerar Nova Prova com IA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Campo de Busca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por Aluno, Matéria ou Título...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Filtros Rápido por Aluno / Matéria
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "TODAS" to "📌 Todas",
                        "ALUNO" to "🧑‍🎓 Por Aluno",
                        "MATERIA" to "📚 Por Matéria"
                    )
                    filters.forEach { (cat, label) ->
                        val isSelected = filterCategory == cat
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color(0xFF1E88E5) else Color(0xFFE3F2FD),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { filterCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF1E88E5)
                            )
                        }
                    }
                }
            }
        }

        if (filteredProvas.isEmpty()) {
            item {
                ProvalinoEmptyState(
                    title = if (searchQuery.isBlank()) "Sua Biblioteca de Provas está Vazia" else "Nenhum resultado encontrado",
                    description = if (searchQuery.isBlank()) "Sua coleção de avaliações e atividades adaptadas ficará salva aqui! Que tal gerar sua primeira prova completa agora?" else "Não encontramos resultados para '$searchQuery'. Tente ajustar sua busca.",
                    buttonText = if (searchQuery.isBlank()) "✨ Gerar Prova com Provalino AI" else null,
                    onButtonClick = if (searchQuery.isBlank()) { { showCreateProvaDialog = true } } else null
                )
            }
        } else {
            items(filteredProvas) { prova ->
                val associatedTurma = turmas.find { it.id == prova.turmaId }
                val qCount = if (prova.questoesIds.isBlank()) 0 else prova.questoesIds.split(",").size

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVerDetalhes(prova) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prova.titulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0D47A1)
                                )
                                if (prova.descricao.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = prova.descricao,
                                        fontSize = 13.sp,
                                        color = Color(0xFF555555),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteProva(prova.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Deletar Prova",
                                    tint = Color(0xFFE53935)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Badges da Prova
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("✅ $qCount Questões Prontas", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }

                                associatedTurma?.let { t ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("🏫 ${t.nome}", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Acessar / Imprimir", fontSize = 12.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Ver Prova",
                                    tint = Color(0xFF1E88E5),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ad Banner Component
        item {
            AdBannerView(
                title = "Provas Adaptadas & AdMob",
                description = "Monetização e parcerias ativas para manter o Provalino AI gratuito para professores."
            )
        }
    }

    if (showCreateProvaDialog) {
        val bnccSubjects = listOf(
            "Língua Portuguesa", "Matemática", "Ciências",
            "História", "Geografia", "Artes", "Educação Física", "Inglês"
        )
        val seriesOptions = listOf(
            "1º Ano Fundamental", "2º Ano Fundamental", "3º Ano Fundamental",
            "4º Ano Fundamental", "5º Ano Fundamental", "6º Ano Fundamental",
            "7º Ano Fundamental", "8º Ano Fundamental", "9º Ano Fundamental",
            "1º Ano Ensino Médio", "2º Ano Ensino Médio", "3º Ano Ensino Médio"
        )
        val perfisAdaptacao = listOf(
            "REGULAR" to "🎓 Regular / Turma Toda",
            "TEA" to "🧩 TEA (Autismo / DUA)",
            "TDAH" to "⚡ TDAH (Foco e Síntese)",
            "DISLEXIA" to "📖 Dislexia (Leitura Clara)",
            "SUPORTE_COGNITIVO" to "🌸 Apoio Cognitivo / Down",
            "ACESSIBILIDADE_VISUAL" to "👁️ Acessibilidade Visual",
            "ACESSIBILIDADE_LINGUISTICA" to "👂 Acessibilidade Linguística",
            "SUPORTE_MULTISSENSORIAL" to "🤝 Multissensorial",
            "ALTAS_HABILIDADES" to "🚀 Altas Habilidades / Superdotação"
        )

        AlertDialog(
            onDismissRequest = { showCreateProvaDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✨", fontSize = 24.sp)
                    Text("Gerar Prova Pronta com Provalino AI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Alternância: Aluno da Carteira vs Geração para Turma/Geral
                    if (alunos.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { generationMode = "ALUNO" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (generationMode == "ALUNO") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (generationMode == "ALUNO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("🧑‍🎓 Aluno da Sala", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { generationMode = "GERAL" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (generationMode == "GERAL") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (generationMode == "GERAL") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("🏫 Turma / Geral", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (generationMode == "ALUNO" && alunos.isNotEmpty()) {
                        Text("Selecione o Aluno Inclusivo:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            alunos.forEach { aluno ->
                                val isSelected = selectedAluno?.id == aluno.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedAluno = aluno
                                            selectedSerie = aluno.serieAno.ifBlank { "3º Ano Fundamental" }
                                            selectedPerfil = aluno.necessidade
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(aluno.avatarEmoji.ifBlank { "🧩" }, fontSize = 18.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(aluno.nome, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${aluno.serieAno} • ${aluno.necessidade} (${aluno.nivelSuporte})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedAluno = aluno
                                            selectedSerie = aluno.serieAno.ifBlank { "3º Ano Fundamental" }
                                            selectedPerfil = aluno.necessidade
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Modo Geral / Perfil Personalizado
                        Text("Série / Ano Escolar:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedSerie = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedSerie, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedSerie,
                                onDismissRequest = { expandedSerie = false }
                            ) {
                                seriesOptions.forEach { serie ->
                                    DropdownMenuItem(
                                        text = { Text(serie, fontSize = 12.sp) },
                                        onClick = {
                                            selectedSerie = serie
                                            expandedSerie = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("Perfil Pedagógico da Prova:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            perfisAdaptacao.forEach { (code, label) ->
                                val isSel = selectedPerfil == code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedPerfil = code }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSel, onClick = { selectedPerfil = code })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Matéria BNCC
                    Text("Matéria / Componente Curricular (BNCC):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bnccSubjects.forEach { mat ->
                            val isSel = selectedMateria == mat
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { selectedMateria = mat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(mat, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Assunto / Conteúdo
                    OutlinedTextField(
                        value = customAssunto,
                        onValueChange = { customAssunto = it },
                        label = { Text("Conteúdo / Assunto (Ex: Interpretação de Texto, Frações)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Quantidade de Questões
                    OutlinedTextField(
                        value = qtdQuestoes,
                        onValueChange = { qtdQuestoes = it },
                        label = { Text("Qtd de Questões (1 a 20)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = qtdQuestoes.toIntOrNull() ?: 4
                        if (count !in 1..20) {
                            Toast.makeText(context, "Digite uma quantidade entre 1 e 20 questões.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (customAssunto.isBlank()) {
                            Toast.makeText(context, "Por favor, descreva o conteúdo ou assunto da prova.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val finalSubject = "$selectedMateria - $customAssunto"
                        if (generationMode == "ALUNO" && selectedAluno != null) {
                            val aluno = selectedAluno!!
                            viewModel.generateAndCreateProvaForAluno(
                                aluno = aluno,
                                subject = finalSubject,
                                grade = aluno.serieAno.ifBlank { selectedSerie },
                                count = count
                            )
                            Toast.makeText(context, "Gerando prova adaptada para ${aluno.nome}...", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generateAndCreateProvaCustom(
                                titulo = "Avaliação: $selectedMateria",
                                subject = finalSubject,
                                grade = selectedSerie,
                                profile = selectedPerfil,
                                count = count
                            )
                            Toast.makeText(context, "Gerando avaliação completa com Provalino AI...", Toast.LENGTH_SHORT).show()
                        }
                        showCreateProvaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text("✨ Gerar Prova Completa (2 🪙)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProvaDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- 5. NOVA PROVA SCREEN ---

@Composable
fun NovaProvaScreen(
    viewModel: ProvalinoViewModel,
    turmas: List<Turma>,
    questoes: List<Questao>
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var selectedTurmaId by remember { mutableStateOf<Int?>(null) }
    val selectedQuestions by viewModel.selectedQuestions.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📝 Nova Prova",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E88E5)
                    )

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título da Avaliação (Ex: Prova Mensal de Ciências)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nova_prova_input_titulo"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Instruções ou Descrição (Opcional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nova_prova_input_desc"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Vincular a uma Turma:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (turmas.isEmpty()) {
                        Text(
                            "Você não possui turmas cadastradas. Cadastre na aba 'Turmas' para organizar.",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            turmas.forEach { t ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selectedTurmaId == t.id) Color(0xFF1E88E5) else Color(0xFFE3F2FD),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedTurmaId = if (selectedTurmaId == t.id) null else t.id }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = t.nome,
                                        color = if (selectedTurmaId == t.id) Color.White else Color(0xFF1E88E5),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selecione as Questões (${selectedQuestions.size} selecionadas)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF333333)
                )

                if (selectedQuestions.isNotEmpty()) {
                    Text(
                        text = "Limpar Seleção",
                        color = Color(0xFFE53935),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.clearQuestionSelection() }
                    )
                }
            }
        }

        if (questoes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📚", fontSize = 40.sp)
                        Text(
                            text = "Nenhuma questão disponível para selecionar.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF777777)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Por favor, adicione ou gere questões na aba 'Questões' antes de montar a prova.",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(questoes) { q ->
                val isSelected = selectedQuestions.contains(q.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.toggleQuestionSelection(q.id) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFEEEEEE),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionada",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    val tLabel = when (q.tipo) {
                                        "MULTIPLE_CHOICE" -> "Múltipla Escolha"
                                        "TRUE_FALSE" -> "Verdadeiro / Falso"
                                        else -> "Questão Discursiva"
                                    }
                                    Text(tLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = q.enunciado,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        Toast.makeText(context, "Por favor, dê um título para a prova.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedQuestions.isEmpty()) {
                        Toast.makeText(context, "Selecione pelo menos 1 questão.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.createProva(titulo, descricao, selectedTurmaId)
                    Toast.makeText(context, "Prova '$titulo' criada com sucesso!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("nova_prova_btn_concluir"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Salvar e Concluir Prova", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- 6. DETALHES PROVA SCREEN ---

@Composable
fun DetalhesProvaScreen(
    viewModel: ProvalinoViewModel,
    prova: Prova,
    turmas: List<Turma>,
    grades: List<NotaAluno>,
    onVoltar: () -> Unit
) {
    var detailTab by remember { mutableIntStateOf(0) }
    var loadedQuestoes by remember { mutableStateOf<List<Questao>>(emptyList()) }
    var nomeEscola by remember { mutableStateOf("Escola Municipal / Estadual Provalino") }
    var showBancoDialog by remember { mutableStateOf(false) }
    val dbQuestoes by viewModel.questoes.collectAsState()
    var selectedBancoQuestao by remember { mutableStateOf<Questao?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val associatedTurma = turmas.find { it.id == prova.turmaId }

    remember(prova) {
        coroutineScope.launch {
            loadedQuestoes = viewModel.getQuestoesForProva(prova)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        // Top Header - Compacto e elegante para focar na prova
        ClayCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            backgroundColor = Color.White,
            borderColor = ClayColors.TealLight,
            shape = RoundedCornerShape(16.dp),
            elevation = 3.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📝 ${prova.titulo}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = ClayColors.TealDark,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ClayButton(
                        onClick = onVoltar,
                        backgroundColor = ClayColors.GreyLight,
                        darkShadowColor = ClayColors.GreyDark,
                        contentColor = ClayColors.TextPrimary,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("↩ Voltar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                ClayTextField(
                    value = nomeEscola,
                    onValueChange = { nomeEscola = it },
                    label = "Nome da Escola (cabeçalho da prova)",
                    placeholder = "Ex: E.E. Professor João da Silva",
                    accentColor = ClayColors.Teal
                )
            }
        }

        // Tactile Clay Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClayTabButton(
                selected = detailTab == 0,
                onClick = { detailTab = 0 },
                title = "Visualização & PDF",
                icon = "👁️",
                selectedColor = ClayColors.Green,
                selectedDarkShadow = ClayColors.GreenDark,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("tab_detalhe_visualizar")
            )
            ClayTabButton(
                selected = detailTab == 1,
                onClick = { detailTab = 1 },
                title = "Dar Notas",
                icon = "✍️",
                selectedColor = ClayColors.Blue,
                selectedDarkShadow = ClayColors.BlueDark,
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_detalhe_corrigir")
            )
            ClayTabButton(
                selected = detailTab == 2,
                onClick = { detailTab = 2 },
                title = "Boletim (${grades.size})",
                icon = "📊",
                selectedColor = ClayColors.Orange,
                selectedDarkShadow = ClayColors.OrangeDark,
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_detalhe_boletim")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (detailTab) {
                0 -> { // --- VISUALIZAR E EXPORTAR PDF ---
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Action Buttons: Imprimir, Compartilhar PDF, Compartilhar Texto e Copiar
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ClayButton(
                                    onClick = {
                                        val htmlContent = buildHtmlExamContent(prova, associatedTurma, loadedQuestoes, nomeEscola)
                                        val webView = WebView(context).apply {
                                            settings.javaScriptEnabled = true
                                            setBackgroundColor(android.graphics.Color.WHITE)
                                            loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
                                        }
                                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                                        val jobName = "${prova.titulo} - Provalino"
                                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                                        printManager.print(jobName, printAdapter, PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build())
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = ClayColors.Red,
                                    darkShadowColor = ClayColors.RedDark,
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("🖨️ Imprimir", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                }

                                ClayButton(
                                    onClick = {
                                        PdfShareHelper.shareExamAsPdf(
                                            context = context,
                                            prova = prova,
                                            turma = associatedTurma,
                                            questoes = loadedQuestoes,
                                            nomeEscola = nomeEscola
                                        )
                                    },
                                    modifier = Modifier.weight(1.2f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = ClayColors.Blue,
                                    darkShadowColor = ClayColors.BlueDark,
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartilhar PDF", tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("📤 PDF", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                }

                                ClayButton(
                                    onClick = {
                                        val text = buildPrintableExamText(prova, associatedTurma, loadedQuestoes, nomeEscola)
                                        val sendIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, text)
                                            type = "text/plain"
                                        }
                                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartilhar Prova Adaptada (Texto)")
                                        context.startActivity(shareIntent)
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = ClayColors.Teal,
                                    darkShadowColor = ClayColors.TealDark,
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("💬 Texto", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                }

                                ClayButton(
                                    onClick = {
                                        val text = buildPrintableExamText(prova, associatedTurma, loadedQuestoes, nomeEscola)
                                        clipboardManager.setText(AnnotatedString(text))
                                        Toast.makeText(context, "Prova copiada para a área de transferência!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.weight(0.9f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = ClayColors.Green,
                                    darkShadowColor = ClayColors.GreenDark,
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("📋 Copiar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            ClayButton(
                                onClick = { showBancoDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = ClayColors.Purple,
                                darkShadowColor = ClayColors.PurpleDark
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Incluir", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("➕ Incluir Questões do Banco de Dados", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // Visual Preview Sheet (A4 Paper Style)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(1.5.dp, ClayColors.GreyLight)
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Header with Provalino Logo
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(text = nomeEscola.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                                            Text(text = "AVALIAÇÃO ADAPTADA & INCLUSIVA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E88E5))
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("🦉", fontSize = 24.sp)
                                            Text("PROVALINO", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF5D4037))
                                        }
                                    }

                                    HorizontalDivider(color = Color.DarkGray, thickness = 1.5.dp)

                                    // Info box (No student name line and no instructions as requested!)
                                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "Data: ____/____/_______  |  Turma: ${associatedTurma?.nome ?: "Geral"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                        Text(text = "Matéria: ${associatedTurma?.materia ?: "Geral"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(text = prova.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                                    // Questions List
                                    loadedQuestoes.forEachIndexed { idx, q ->
                                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Questão ${idx + 1}: ${q.enunciado}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            val isCaaProfile = q.perfilAdaptacao.uppercase() in listOf(
                                                "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
                                            )
                                            if (isCaaProfile && q.pictogramasSuporte.isNotBlank()) {
                                                PictogramSupportRow(
                                                    rawPictogramText = q.pictogramasSuporte,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                            if (q.tipo == "MULTIPLE_CHOICE") {
                                                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                                                    if (q.opcaoA.isNotBlank()) Text(text = "A) ${formatOptionText(q.opcaoA, "A")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                                    if (q.opcaoB.isNotBlank()) Text(text = "B) ${formatOptionText(q.opcaoB, "B")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                                    if (q.opcaoC.isNotBlank()) Text(text = "C) ${formatOptionText(q.opcaoC, "C")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                                    if (q.opcaoD.isNotBlank()) Text(text = "D) ${formatOptionText(q.opcaoD, "D")}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                                }
                                            } else if (q.tipo == "TRUE_FALSE") {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Box(modifier = Modifier.size(18.dp).border(1.5.dp, Color(0xFF0F172A), RoundedCornerShape(4.dp)))
                                                        Text("Verdadeiro (V)", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Box(modifier = Modifier.size(18.dp).border(1.5.dp, Color(0xFF0F172A), RoundedCornerShape(4.dp)))
                                                        Text("Falso (F)", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                                                    Text("[Resposta do Aluno]:", fontSize = 11.sp, color = Color(0xFF475569), fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium)
                                                    Box(modifier = Modifier.fillMaxWidth().height(65.dp).background(Color(0xFFFAFAFA), RoundedCornerShape(6.dp)).border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(6.dp)))
                                                }
                                            }
                                        }
                                        if (idx < loadedQuestoes.size - 1) {
                                            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.LightGray)
                                    // Footer phrase
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Prova gerada utilizando o aplicativo Provalino - Atividades Adaptadas",
                                            fontSize = 10.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = Color(0xFF777777),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // --- DAR NOTAS ---
                    item {
                        GradingForm(
                            prova = prova,
                            questoes = loadedQuestoes,
                            onSaveGrade = { nomeAluno, respostasMap ->
                                viewModel.salvarNotaAluno(prova.id, nomeAluno, respostasMap, loadedQuestoes)
                            }
                        )
                    }
                }
                2 -> { // --- BOLETIM ---
                    item {
                        GradesReportList(grades = grades, viewModel = viewModel, prova = prova)
                    }
                }
            }
        }
    }

    if (showBancoDialog) {
        val currentIds = prova.questoesIds.split(",").mapNotNull { it.toIntOrNull() }
        val filteredDbQuestoes = dbQuestoes.filter { q -> !currentIds.contains(q.id) }

        AlertDialog(
            onDismissRequest = { 
                showBancoDialog = false
                selectedBancoQuestao = null
            },
            title = {
                Text("📚 Banco de Questões Salvas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF8E24AA))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedBancoQuestao != null) {
                        val q = selectedBancoQuestao!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Visualização Completa da Questão:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6A1B9A))
                                Text(q.enunciado, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                if (q.tipo == "MULTIPLE_CHOICE") {
                                    if (q.opcaoA.isNotBlank()) Text("A) ${q.opcaoA}", fontSize = 12.sp)
                                    if (q.opcaoB.isNotBlank()) Text("B) ${q.opcaoB}", fontSize = 12.sp)
                                    if (q.opcaoC.isNotBlank()) Text("C) ${q.opcaoC}", fontSize = 12.sp)
                                    if (q.opcaoD.isNotBlank()) Text("D) ${q.opcaoD}", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { selectedBancoQuestao = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78909C)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Voltar", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.appendQuestaoToProva(prova.id, q.id)
                                            coroutineScope.launch {
                                                loadedQuestoes = viewModel.getQuestoesForProva(prova)
                                            }
                                            Toast.makeText(context, "Questão incluída com sucesso na prova!", Toast.LENGTH_SHORT).show()
                                            selectedBancoQuestao = null
                                            showBancoDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Incluir", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        if (filteredDbQuestoes.isEmpty()) {
                            Text("Nenhuma questão nova disponível no banco de dados.", fontSize = 13.sp, color = Color(0xFF666666))
                        } else {
                            Text("Selecione uma questão para visualizar e incluir:", fontSize = 12.sp, color = Color(0xFF666666))
                            filteredDbQuestoes.forEach { q ->
                                val summary = if (q.enunciado.length > 45) q.enunciado.take(45) + "..." else q.enunciado
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBancoQuestao = q },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "📌 $summary",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF333333),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Matéria: ${q.assunto.ifBlank { "Geral" }} | Série: ${q.anoEscolar.ifBlank { "Geral" }}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF777777)
                                            )
                                        }
                                        Text(">", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF8E24AA))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showBancoDialog = false
                    selectedBancoQuestao = null
                }) {
                    Text("Fechar")
                }
            }
        )
    }
}

fun formatOptionText(raw: String, prefixLetter: String): String {
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

fun buildHtmlExamContent(prova: Prova, turma: Turma?, questoes: List<Questao>, escola: String): String {
    val sb = java.lang.StringBuilder()
    sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
    sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
    sb.append("<style>")
    sb.append("@media print { body { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; } }")
    sb.append("* { box-sizing: border-box; }")
    sb.append("body { font-family: Arial, Helvetica, sans-serif; padding: 20px; color: #000000 !important; background-color: #ffffff !important; line-height: 1.4; margin: 0; }")
    sb.append(".header { border-bottom: 2px solid #111827; padding-bottom: 10px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }")
    sb.append(".escola { font-size: 15px; font-weight: bold; color: #000000 !important; }")
    sb.append(".sub-header { font-size: 12px; font-weight: bold; color: #0284C7 !important; }")
    sb.append(".logo { font-size: 13px; font-weight: bold; color: #5D4037 !important; }")
    sb.append(".info { background: #F1F5F9; padding: 10px 12px; border-radius: 6px; margin-bottom: 18px; font-size: 12px; font-weight: bold; color: #000000 !important; border: 1px solid #CBD5E1; }")
    sb.append(".titulo { font-size: 16px; font-weight: bold; text-align: center; margin-bottom: 18px; color: #000000 !important; }")
    sb.append(".questao { margin-bottom: 20px; font-size: 13px; color: #000000 !important; clear: both; page-break-inside: avoid; }")
    sb.append(".enunciado { font-weight: bold; font-size: 13px; color: #000000 !important; line-height: 1.5; }")
    sb.append(".caa-box { margin: 8px 0 10px 0; padding: 8px 10px; background-color: #F8FAFC; border: 1.5px solid #CBD5E1; border-radius: 8px; clear: both; }")
    sb.append(".caa-title { font-size: 11px; font-weight: 800; color: #0F766E !important; margin-bottom: 6px; }")
    sb.append(".caa-cards { display: flex; flex-wrap: wrap; gap: 8px; }")
    sb.append(".caa-card { display: inline-block; background: #FFFFFF; border: 1.5px solid #0F766E; border-radius: 6px; padding: 4px 8px; text-align: center; min-width: 55px; }")
    sb.append(".caa-sym { font-size: 22px; line-height: 26px; }")
    sb.append(".caa-lbl { font-size: 9px; font-weight: 800; color: #0F172A !important; margin-top: 2px; }")
    sb.append(".opcoes-box { margin: 8px 0 4px 8px; clear: both; }")
    sb.append(".opcao { font-size: 13px; font-weight: 600; color: #000000 !important; margin: 5px 0; line-height: 1.4; }")
    sb.append(".tf-row { font-size: 13px; font-weight: bold; color: #000000 !important; margin: 8px 0 6px 8px; clear: both; }")
    sb.append(".discursiva-box { border: 1.5px solid #94A3B8; height: 70px; margin-top: 6px; border-radius: 6px; background-color: #FAFAFA; clear: both; }")
    sb.append(".footer { margin-top: 35px; text-align: center; font-size: 10px; color: #555555 !important; font-style: italic; border-top: 1px solid #CBD5E1; padding-top: 8px; clear: both; }")
    sb.append("</style></head><body>")
    sb.append("<div class='header'><div><div class='escola'>" + escola.uppercase() + "</div><div class='sub-header'>AVALIAÇÃO ADAPTADA & INCLUSIVA</div></div><div class='logo'>🦉 PROVALINO</div></div>")
    sb.append("<div class='info'>Data: ____/____/_______ &nbsp;|&nbsp; Turma: " + (turma?.nome ?: "Geral") + " &nbsp;|&nbsp; Matéria: " + (turma?.materia ?: "Geral") + "</div>")
    sb.append("<div class='titulo'>" + prova.titulo + "</div>")
    questoes.forEachIndexed { index, q ->
        sb.append("<div class='questao'>")
        val cleanEnunciado = PictogramInjector.removeTechnicalTags(q.enunciado)
        sb.append("<div class='enunciado'><b>Questão " + (index + 1) + ":</b> " + cleanEnunciado + "</div>")
        val isCaaProfile = q.perfilAdaptacao.uppercase() in listOf(
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
        )
        if (isCaaProfile && q.pictogramasSuporte.isNotBlank()) {
            val terms = PictogramInjector.extractTerms(q.pictogramasSuporte)
            if (terms.isNotEmpty()) {
                sb.append("<div class='caa-box'>")
                sb.append("<div class='caa-title'>🧩 CARTÕES VISUAIS DE APOIO (CAA):</div>")
                sb.append("<div class='caa-cards'>")
                terms.forEach { term ->
                    val local = PictogramCatalog.find(term)
                    val sym = local?.symbol ?: PictogramCatalog.resolveSymbol(term)
                    val rawLbl = local?.label ?: term
                    val lbl = rawLbl
                        .replace(Regex("""\[(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card)(?:[/\s\-_]+(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card))?:\s*""", RegexOption.IGNORE_CASE), "")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("ARASAAC:", "")
                        .replace(Regex("""[\p{So}\p{Sk}\p{Sm}\p{Cs}\p{Cn}]"""), "")
                        .trim()
                        .uppercase()
                    if (lbl.isNotBlank()) {
                        sb.append("<div class='caa-card'>")
                        sb.append("<div class='caa-sym'>").append(sym).append("</div>")
                        sb.append("<div class='caa-lbl'>").append(lbl).append("</div>")
                        sb.append("</div>")
                    }
                }
                sb.append("</div></div>")
            }
        }
        if (q.tipo == "MULTIPLE_CHOICE") {
            sb.append("<div class='opcoes-box'>")
            if (q.opcaoA.isNotBlank()) sb.append("<div class='opcao'><b>A)</b> " + formatOptionText(q.opcaoA, "A") + "</div>")
            if (q.opcaoB.isNotBlank()) sb.append("<div class='opcao'><b>B)</b> " + formatOptionText(q.opcaoB, "B") + "</div>")
            if (q.opcaoC.isNotBlank()) sb.append("<div class='opcao'><b>C)</b> " + formatOptionText(q.opcaoC, "C") + "</div>")
            if (q.opcaoD.isNotBlank()) sb.append("<div class='opcao'><b>D)</b> " + formatOptionText(q.opcaoD, "D") + "</div>")
            sb.append("</div>")
        } else if (q.tipo == "TRUE_FALSE") {
            sb.append("<div class='tf-row'>&nbsp;&nbsp;<b>( &nbsp; ) Verdadeiro (V)</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>( &nbsp; ) Falso (F)</b></div>")
        } else {
            sb.append("<div style='margin-top: 6px; font-size: 11px; color: #475569; font-style: italic;'>[Questão Discursiva - Escreva sua resposta abaixo]:</div><div class='discursiva-box'></div>")
        }
        sb.append("</div>")
    }
    sb.append("<div class='footer'>Prova gerada utilizando o aplicativo Provalino - Atividades Adaptadas</div>")
    sb.append("</body></html>")
    return sb.toString()
}

fun buildPrintableExamText(prova: Prova, turma: Turma?, questoes: List<Questao>, escola: String): String {
    val sb = java.lang.StringBuilder()
    sb.append("===================================================\n")
    sb.append("ESCOLA: " + escola.uppercase() + "\n")
    sb.append("DATA: ____/____/_______   |   TURMA: " + (turma?.nome ?: "Geral") + "\n")
    sb.append("MATÉRIA: " + (turma?.materia ?: "Geral") + "\n")
    sb.append("===================================================\n\n")
    sb.append("                 " + prova.titulo.uppercase() + "\n\n")
    questoes.forEachIndexed { index, q ->
        sb.append("QUESTÃO " + (index + 1) + ": " + q.enunciado + "\n")
        val isCaaProfile = q.perfilAdaptacao.uppercase() in listOf(
            "TEA", "AUTISMO", "DEF_INTELECTUAL", "SUPORTE_COGNITIVO", "SINDROME_DOWN"
        )
        val cleanSupport = PictogramInjector.cleanSupportText(q.pictogramasSuporte)
        if (isCaaProfile && cleanSupport.isNotBlank()) {
            sb.append("  [Suporte Visual (CAA): " + cleanSupport + "]\n")
        }
        if (q.tipo == "MULTIPLE_CHOICE") {
            sb.append("  A) " + formatOptionText(q.opcaoA, "A") + "\n")
            sb.append("  B) " + formatOptionText(q.opcaoB, "B") + "\n")
            sb.append("  C) " + formatOptionText(q.opcaoC, "C") + "\n")
            sb.append("  D) " + formatOptionText(q.opcaoD, "D") + "\n")
        } else if (q.tipo == "TRUE_FALSE") {
            sb.append("  (   ) Verdadeiro       (   ) Falso\n")
        } else {
            sb.append("  [Questão Discursiva - Escreva sua resposta nas linhas abaixo]\n\n\n___________________________________________________\n")
        }
        sb.append("\n")
    }
    sb.append("---------------------------------------------------\n")
    sb.append("Prova gerada utilizando o aplicativo Provalino - Atividades Adaptadas\n")
    return sb.toString()
}
@Composable
fun GradingForm(
    prova: Prova,
    questoes: List<Questao>,
    onSaveGrade: (String, Map<Int, String>) -> Unit
) {
    var nomeAluno by remember { mutableStateOf("") }
    val respostasMap = remember { mutableStateMapOf<Int, String>() }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "📝 Corretor de Provas Rápido",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E88E5)
        )
        Text(
            text = "Diga qual foi a resposta que o aluno marcou para cada questão. O Provalino calcula a nota automaticamente!",
            fontSize = 12.sp,
            color = Color.Gray
        )

        OutlinedTextField(
            value = nomeAluno,
            onValueChange = { nomeAluno = it },
            label = { Text("Nome Completo do Aluno") },
            modifier = Modifier.fillMaxWidth().testTag("grade_input_nome"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (questoes.isEmpty()) {
            Text("Nenhuma questão nesta prova para avaliar.", color = Color.Red, fontSize = 12.sp)
        } else {
            questoes.forEachIndexed { index, q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Questão ${index + 1}: ${q.enunciado}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val currentAns = respostasMap[q.id] ?: ""

                        if (q.tipo == "MULTIPLE_CHOICE") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("A", "B", "C", "D").forEach { option ->
                                    val isChosen = currentAns == option
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isChosen) Color(0xFF1E88E5) else Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                                            .clickable { respostasMap[q.id] = option }
                                            .weight(1f)
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        } else if (q.tipo == "TRUE_FALSE") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("V", "F").forEach { option ->
                                    val isChosen = currentAns == option
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isChosen) Color(0xFF1E88E5) else Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                                            .clickable { respostasMap[q.id] = option }
                                            .weight(1f)
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (option == "V") "Verdadeiro (V)" else "Falso (F)",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            // DISCURSIVA (ABERTA)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val isCorreto = currentAns == "C"
                                val isIncorreto = currentAns == "I"

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isCorreto) Color(0xFF4CAF50) else Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                                        .clickable { respostasMap[q.id] = "C" }
                                        .weight(1f)
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✅ Correto",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorreto) Color.White else Color(0xFF4CAF50)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isIncorreto) Color(0xFFE53935) else Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                                        .clickable { respostasMap[q.id] = "I" }
                                        .weight(1f)
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "❌ Errado",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncorreto) Color.White else Color(0xFFE53935)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (nomeAluno.isBlank()) {
                        Toast.makeText(context, "Por favor, digite o nome do aluno.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (respostasMap.size < questoes.size) {
                        Toast.makeText(context, "Responda todas as questões do aluno.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onSaveGrade(nomeAluno, respostasMap.toMap())
                    Toast.makeText(context, "Nota do aluno $nomeAluno salva com sucesso!", Toast.LENGTH_SHORT).show()

                    // Reset form
                    nomeAluno = ""
                    respostasMap.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("grade_btn_salvar"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Calcular e Salvar Nota", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// --- HELPER DE FORMATAÇÃO DO TEXTO DA PROVA PARA COPIAR ---

fun buildPrintableExamText(prova: Prova, turma: Turma?, questoes: List<Questao>): String {
    val sb = java.lang.StringBuilder()
    sb.append("===================================================\n")
    sb.append("ESCOLA: ___________________________________________\n")
    sb.append("NOME DO ALUNO: ____________________________________\n")
    sb.append("DATA: ____/____/_______   |   SÉRIE/TURMA: ${turma?.nome ?: "__________"}\n")
    sb.append("MATÉRIA: ${turma?.materia ?: "__________"}\n")
    sb.append("===================================================\n")
    sb.append("\n")
    sb.append("                 ${prova.titulo.uppercase()}\n")
    sb.append("\n")
    if (prova.descricao.isNotBlank()) {
        sb.append("Instruções: ${prova.descricao}\n\n")
    }

    questoes.forEachIndexed { index, q ->
        sb.append("QUESTÃO ${index + 1}: ${q.enunciado}\n")
        if (q.tipo == "MULTIPLE_CHOICE") {
            sb.append("  A) ${formatOptionText(q.opcaoA, "A")}\n")
            sb.append("  B) ${formatOptionText(q.opcaoB, "B")}\n")
            sb.append("  C) ${formatOptionText(q.opcaoC, "C")}\n")
            sb.append("  D) ${formatOptionText(q.opcaoD, "D")}\n")
        } else if (q.tipo == "TRUE_FALSE") {
            sb.append("  (   ) Verdadeiro (V)\n")
            sb.append("  (   ) Falso (F)\n")
        } else {
            sb.append("\n\n\n___________________________________________________\n\n")
        }
        sb.append("\n")
    }

    sb.append("\n")
    sb.append("===================================================\n")
    sb.append("                GABARITO OFICIAL (PROFESSOR)       \n")
    sb.append("===================================================\n")
    sb.append("\n")
    questoes.forEachIndexed { index, q ->
        sb.append("QUESTÃO ${index + 1}: Resposta correta -> ${q.respostaCorreta}\n")
    }
    sb.append("\n===================================================\n")

    return sb.toString()
}

fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

// --- CARTEIRA DE INCLUSÃO DOS ALUNOS COM NECESSIDADES ESPECIAIS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarteirasScreen(
    viewModel: ProvalinoViewModel,
    alunos: List<com.example.data.AlunoNecessidadeEspecial>
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlunoId by remember { mutableStateOf<Int?>(null) }
    var expandedSerie by remember { mutableStateOf(false) }
    var expandedNecessidade by remember { mutableStateOf(false) }
    var expandedSeveridade by remember { mutableStateOf(false) }

    // Form fields (iniciam vazios para novo cadastro)
    var nome by remember { mutableStateOf("") }
    var serieAno by remember { mutableStateOf("") }
    var necessidade by remember { mutableStateOf("") }
    var nivelSuporte by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var avatarEmoji by remember { mutableStateOf("🧩") }

    // AI Activity Generation Dialog State for Specific Student
    var alunoParaGerarAtividade by remember { mutableStateOf<com.example.data.AlunoNecessidadeEspecial?>(null) }
    var selectedMateria by remember { mutableStateOf("Matemática") }
    var selectedAssuntoBNCC by remember { mutableStateOf("Números e Operações") }
    var customAssunto by remember { mutableStateOf("") }
    var qtdQuestoes by remember { mutableStateOf("3") }

    val bnccSubjectsMap = mapOf(
        "Matemática" to listOf("Números e Operações", "Geometria", "Grandezas e Medidas", "Probabilidade e Estatística", "Álgebra"),
        "Língua Portuguesa" to listOf("Leitura e Compreensão de Textos", "Produção de Textos", "Fonética e Ortografia", "Oralidade e Conversação", "Análise Linguística e Semiótica"),
        "Ciências" to listOf("Matéria e Energia", "Vida e Evolução", "Terra e Universo"),
        "História" to listOf("O Mundo Pessoal e a Família", "Tempo, Memória e Identidade", "Povos e Culturas"),
        "Geografia" to listOf("O Lugar e a Paisagem", "Dinâmicas da Natureza e Sociedade", "Cartografia Escolar e Orientação"),
        "Artes" to listOf("Artes Visuais", "Dança", "Música", "Teatro", "Artes Integradas e Expressão Criativa"),
        "Educação Física" to listOf("Brincadeiras e Jogos", "Esportes e Cooperação", "Ginásticas e Coordenação", "Danças e Expressão Corporal", "Práticas Corporais"),
        "Inglês" to listOf("Oralidade e Conversação", "Leitura e Vocabulário", "Escrita e Estruturas Básicas", "Dimensão Intercultural")
    )

    val necessidadesList = listOf(
        "TEA" to "Transtorno do Espectro Autista (TEA) 🧩",
        "SINDROME_DOWN" to "Síndrome de Down 💛",
        "TDAH" to "Transtorno do Déficit de Atenção com Hiperatividade (TDAH) ⚡",
        "DISLEXIA" to "Transtorno Específico da Aprendizagem (Dislexia) 📖",
        "DEF_INTELECTUAL" to "Deficiência Intelectual 🧠",
        "BAIXA_VISAO" to "Baixa Visão / Deficiência Visual 👁️",
        "SURDEZ" to "Surdez / Deficiência Auditiva 👂",
        "SUPORTE_COGNITIVO" to "Apoio Cognitivo e Funcional (AEE) 🌸",
        "ALTAS_HABILIDADES" to "Altas Habilidades / Superdotação 🚀"
    )

    val serieAnoList = listOf(
        "Infantil / Pré-escola",
        "1º Ano Fundamental",
        "2º Ano Fundamental",
        "3º Ano Fundamental",
        "4º Ano Fundamental",
        "5º Ano Fundamental",
        "6º Ano Fundamental",
        "7º Ano Fundamental",
        "8º Ano Fundamental",
        "9º Ano Fundamental"
    )

    val severidadesList = listOf(
        "Nível 1 (Leve / Suporte Baixo)",
        "Nível 2 (Moderado / Suporte Médio)",
        "Nível 3 (Severo / Suporte Intensivo)"
    )

    val avatarList = listOf("🧩", "🧑‍🎓", "👧", "👦", "🎨", "🚀", "🌟", "📚", "⭐")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. LOUSA / QUADRO NEGRO HEADER (Ambiente Escolar Lúdico) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B2B)), // Dark green chalkboard
                border = BorderStroke(3.dp, Color(0xFF5D4037)), // Wooden blackboard frame
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏫 SALA DE AULA INCLUSIVA & AEE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFFFFD54F),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quadro de Alunos nas Carteiras (${alunos.size}/9)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Professor(a), posicione seus alunos nas carteiras escolares para planejar aulas inclusivas (DUA) e gerar atividades adaptadas com IA!",
                        fontSize = 12.sp,
                        color = Color(0xFFB9F6CA),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (alunos.size >= 9) {
                                Toast.makeText(context, "Limite de 9 carteiras na sala atingido! Remova um aluno para cadastrar outro.", Toast.LENGTH_LONG).show()
                            } else {
                                editingAlunoId = null
                                nome = ""
                                serieAno = ""
                                necessidade = ""
                                nivelSuporte = ""
                                observacoes = ""
                                avatarEmoji = "🧩"
                                showAddDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_add_aluno_inclusao"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("➕ Cadastrar Aluno na Carteira (${alunos.size}/9)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // --- 2. SALA DE AULA - DISPOSIÇÃO DE CARTEIRAS (Grade de 2 Colunas) ---
        if (alunos.isEmpty()) {
            item {
                ProvalinoEmptyState(
                    title = "Carteira de Alunos Inclusivos Vazia",
                    description = "Cadastre seus alunos com necessidades educacionais especiais para que a IA do Provalino crie atividades perfeitamente adaptadas ao perfil pedagógico de cada estudante!",
                    buttonText = "➕ Posicionar Aluno na Carteira",
                    onButtonClick = {
                        editingAlunoId = null
                        nome = ""
                        serieAno = ""
                        necessidade = ""
                        nivelSuporte = ""
                        observacoes = ""
                        avatarEmoji = "🧩"
                        showAddDialog = true
                    }
                )
            }
        } else {
            val rows = alunos.chunked(2)
            items(rows.size) { rowIndex ->
                val rowAlunos = rows[rowIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (aluno in rowAlunos) {
                        // Carteira Escolar Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                width = 2.dp,
                                color = when (aluno.necessidade) {
                                    "TEA" -> Color(0xFF90CAF9)
                                    "SINDROME_DOWN" -> Color(0xFFFFF59D)
                                    "TDAH" -> Color(0xFFFFE082)
                                    "DISLEXIA" -> Color(0xFFCE93D8)
                                    "DEF_INTELECTUAL" -> Color(0xFFFFCC80)
                                    "BAIXA_VISAO", "ACESSIBILIDADE_VISUAL" -> Color(0xFFD7CCC8)
                                    "SURDEZ", "ACESSIBILIDADE_LINGUISTICA" -> Color(0xFFB2DFDB)
                                    "SUPORTE_COGNITIVO" -> Color(0xFFFFAB91)
                                    "SUPORTE_MULTISSENSORIAL" -> Color(0xFFF8BBD0)
                                    "ALTAS_HABILIDADES" -> Color(0xFFB9F6CA)
                                    else -> Color(0xFF80CBC4)
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFE8F5E9), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(aluno.avatarEmoji.ifBlank { "🧩" }, fontSize = 18.sp)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = aluno.nome,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF333333),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = aluno.serieAno,
                                                fontSize = 10.sp,
                                                color = Color(0xFF666666),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Action buttons: Edit (pencil) & Delete (trash)
                                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingAlunoId = aluno.id
                                                nome = aluno.nome
                                                serieAno = aluno.serieAno
                                                necessidade = aluno.necessidade
                                                nivelSuporte = aluno.nivelSuporte
                                                observacoes = aluno.observacoesPedagogicas
                                                avatarEmoji = aluno.avatarEmoji.ifBlank { "🧩" }
                                                showAddDialog = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar Aluno",
                                                tint = Color(0xFF1E88E5),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteAlunoInclusao(aluno.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir Carteira",
                                                tint = Color(0xFFE53935),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Badges
                                val (badgeText, badgeBg, badgeFg) = when (aluno.necessidade) {
                                    "TEA" -> Triple("🧩 TEA", Color(0xFFE3F2FD), Color(0xFF0D47A1))
                                    "SINDROME_DOWN" -> Triple("💛 Síndrome de Down", Color(0xFFFFFDE7), Color(0xFFF57F17))
                                    "TDAH" -> Triple("⚡ TDAH", Color(0xFFFFF8E1), Color(0xFFF57F17))
                                    "DISLEXIA" -> Triple("📖 Dislexia", Color(0xFFF3E5F5), Color(0xFF4A148C))
                                    "DEF_INTELECTUAL" -> Triple("🧠 Def. Intelectual", Color(0xFFFFF3E0), Color(0xFFD84315))
                                    "BAIXA_VISAO", "ACESSIBILIDADE_VISUAL" -> Triple("👁️ Baixa Visão", Color(0xFFEFEBE9), Color(0xFF4E342E))
                                    "SURDEZ", "ACESSIBILIDADE_LINGUISTICA" -> Triple("👂 Surdez / Auditiva", Color(0xFFE0F7FA), Color(0xFF006064))
                                    "SUPORTE_COGNITIVO" -> Triple("🌸 Apoio Cognitivo", Color(0xFFFBE9E7), Color(0xFFD84315))
                                    "SUPORTE_MULTISSENSORIAL" -> Triple("🤝 Multissensorial", Color(0xFFFCE4EC), Color(0xFF880E4F))
                                    "ALTAS_HABILIDADES" -> Triple("🚀 Altas Habilidades", Color(0xFFE0F2F1), Color(0xFF00695C))
                                    else -> Triple(necessidadesList.firstOrNull { it.first == aluno.necessidade }?.second?.take(18) ?: aluno.necessidade, Color(0xFFECEFF1), Color(0xFF37474F))
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(badgeText, fontSize = 10.sp, color = badgeFg, fontWeight = FontWeight.Bold)
                                    }

                                    // Severidade / Nível de Suporte Badge
                                    if (aluno.nivelSuporte.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEDE7F6), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("⚙️ ${aluno.nivelSuporte}", fontSize = 9.sp, color = Color(0xFF4527A0), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                if (aluno.observacoesPedagogicas.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = aluno.observacoesPedagogicas,
                                        fontSize = 11.sp,
                                        color = Color(0xFF555555),
                                        maxLines = 2,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { alunoParaGerarAtividade = aluno },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                                ) {
                                    Text("✨ Gerar Atividade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // If row has only 1 student, add an empty spacer weight to keep row alignment balanced
                    if (rowAlunos.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Ad Banner Component
        item {
            AdBannerView(
                title = "Sala de Aula Inclusiva & AdMob",
                description = "Anúncios parceiros mantêm o cadastro de alunos nas carteiras e DUA/AEE gratuito para professores."
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    if (editingAlunoId != null) "✏️ Editar Aluno na Carteira" else "➕ Cadastrar Aluno na Carteira (${alunos.size + 1}/9)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome Completo do Aluno") },
                        placeholder = { Text("Ex: Lucas Silva") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // 1. Seletor Dropdown da Série / Ano Escolar (Sem pré-seleção)
                    Text("Série / Ano Escolar:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    ExposedDropdownMenuBox(
                        expanded = expandedSerie,
                        onExpandedChange = { expandedSerie = !expandedSerie },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = serieAno,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Série / Ano Escolar") },
                            placeholder = { Text("Selecione a Série / Ano...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSerie) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSerie,
                            onDismissRequest = { expandedSerie = false }
                        ) {
                            serieAnoList.forEach { serie ->
                                DropdownMenuItem(
                                    text = { Text(serie) },
                                    onClick = {
                                        serieAno = serie
                                        expandedSerie = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. Seletor Dropdown da Necessidade Especial / Perfil AEE (Sem pré-seleção)
                    Text("Necessidade Especial / Perfil Pedagógico:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    val selectedNecessidadeLabel = necessidadesList.firstOrNull { it.first == necessidade }?.second ?: ""
                    ExposedDropdownMenuBox(
                        expanded = expandedNecessidade,
                        onExpandedChange = { expandedNecessidade = !expandedNecessidade },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedNecessidadeLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Necessidade Especial") },
                            placeholder = { Text("Selecione a Necessidade Especial...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNecessidade) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedNecessidade,
                            onDismissRequest = { expandedNecessidade = false }
                        ) {
                            necessidadesList.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 13.sp) },
                                    onClick = {
                                        necessidade = code
                                        expandedNecessidade = false
                                    }
                                )
                            }
                        }
                    }

                    // 3. Seletor Dropdown do Grau de Severidade / Suporte (Sem pré-seleção)
                    Text("Grau de Severidade / Suporte:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    ExposedDropdownMenuBox(
                        expanded = expandedSeveridade,
                        onExpandedChange = { expandedSeveridade = !expandedSeveridade },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = nivelSuporte,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grau de Severidade") },
                            placeholder = { Text("Selecione o Grau de Severidade...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeveridade) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSeveridade,
                            onDismissRequest = { expandedSeveridade = false }
                        ) {
                            severidadesList.forEach { sev ->
                                DropdownMenuItem(
                                    text = { Text(sev, fontSize = 13.sp) },
                                    onClick = {
                                        nivelSuporte = sev
                                        expandedSeveridade = false
                                    }
                                )
                            }
                        }
                    }

                    // Seletor de Ícone / Avatar
                    Text("Avatar do Aluno:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        avatarList.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(if (avatarEmoji == emoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .clickable { avatarEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações Pedagógicas (Opcional)") },
                        placeholder = { Text("Ex: Gosta de dinossauros, responde bem a recursos visuais...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nome.isBlank()) {
                            Toast.makeText(context, "Por favor, digite o nome do aluno.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (serieAno.isBlank()) {
                            Toast.makeText(context, "Por favor, selecione a série / ano escolar.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (necessidade.isBlank()) {
                            Toast.makeText(context, "Por favor, selecione a necessidade especial do aluno.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nivelSuporte.isBlank()) {
                            Toast.makeText(context, "Por favor, selecione o grau de severidade / suporte.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val result = viewModel.saveOrUpdateAlunoInclusao(
                            id = editingAlunoId ?: 0,
                            nome = nome,
                            necessidade = necessidade,
                            nivelSuporte = nivelSuporte,
                            observacoesPedagogicas = observacoes,
                            avatarEmoji = avatarEmoji,
                            serieAno = serieAno
                        )
                        if (result) {
                            Toast.makeText(context, if (editingAlunoId != null) "Aluno atualizado na carteira!" else "Aluno salvo na carteira!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                            editingAlunoId = null
                            nome = ""
                            serieAno = ""
                            necessidade = ""
                            nivelSuporte = ""
                            observacoes = ""
                        } else {
                            Toast.makeText(context, "Limite máximo de 9 carteiras atingido!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text(if (editingAlunoId != null) "Atualizar Aluno" else "Salvar Aluno", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingAlunoId = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (alunoParaGerarAtividade != null) {
        val aluno = alunoParaGerarAtividade!!
        AlertDialog(
            onDismissRequest = { alunoParaGerarAtividade = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Column {
                    Text("✨ Gerar Atividade Adaptada", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Aluno: ${aluno.nome} (${aluno.serieAno})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    val displayNecessidade = necessidadesList.firstOrNull { it.first == aluno.necessidade }?.second ?: aluno.necessidade
                    Text("Perfil: $displayNecessidade • ${aluno.nivelSuporte}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Selecione a Matéria:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bnccSubjectsMap.keys.forEach { mat ->
                            val isSel = selectedMateria == mat
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMateria = mat
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(mat, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customAssunto,
                        onValueChange = { customAssunto = it },
                        label = { Text("Descreva o Assunto/Conteúdo da Prova") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = qtdQuestoes,
                        onValueChange = { qtdQuestoes = it },
                        label = { Text("Qtd Questões (1 a 20)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = qtdQuestoes.toIntOrNull() ?: 3
                        if (count !in 1..20) {
                            Toast.makeText(context, "O número de questões deve ser entre 1 e 20.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (customAssunto.isBlank()) {
                            Toast.makeText(context, "Por favor, descreva o assunto da prova.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val finalAssunto = "$selectedMateria - $customAssunto"
                        viewModel.generateAndCreateProvaForAluno(
                            aluno = aluno,
                            subject = finalAssunto,
                            grade = aluno.serieAno,
                            count = count
                        )
                        Toast.makeText(context, "Gerando prova adaptada e pronta para ${aluno.nome}...", Toast.LENGTH_SHORT).show()
                        alunoParaGerarAtividade = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("✨ Gerar Prova Pronta com Provalino AI (2 🪙)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { alunoParaGerarAtividade = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
// --- SIMULATED AD DIALOG FOR COIN MONETIZATION ---

@Composable
fun SimulatedAdDialog(
    adType: String, // "REWARDED" (+3 moedas) or "INTERSTITIAL" (0 moedas - rentabilização)
    adLimitMessage: String? = null,
    onClose: (grantedReward: Boolean) -> Unit
) {
    var timerSeconds by remember { mutableStateOf(5) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            timerSeconds--
        }
        isFinished = true
    }

    val isRewarded = adType == "REWARDED"

    AlertDialog(
        onDismissRequest = { if (isFinished) onClose(isRewarded && adLimitMessage == null) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (isRewarded) "🪙" else "📺", fontSize = 24.sp)
                Text(
                    text = if (isRewarded) "Anúncio Recompensado (Até 10 Moedas/Dia)" else "Anúncio Intersticial (Rentabilização)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!adLimitMessage.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Text(
                                text = adLimitMessage,
                                fontSize = 11.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isRewarded) listOf(Color(0xFF3F51B5), Color(0xFF9C27B0)) else listOf(Color(0xFF4527A0), Color(0xFF00838F))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📺 PROVALINO ADS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isRewarded) "Assista ao anúncio para resgatar moedas (Limite diário: até 10 moedas/dia)!" else "Anúncio de rentabilização para manter o Provalino gratuito.",
                            color = Color(0xFFE8EAF6),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (!isFinished) {
                            Text("Aguarde ${timerSeconds}s ...", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text("🎉 Anúncio Concluído!", color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                if (!isFinished) {
                    LinearProgressIndicator(
                        progress = { (5 - timerSeconds) / 5f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )
                } else {
                    Text(
                        text = if (isRewarded && adLimitMessage == null) "Você assistiu ao anúncio e resgatou moedas para gerar e adaptar mais provas!" else "Obrigado por apoiar a plataforma Provalino de Inclusão Escolar!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onClose(isRewarded && adLimitMessage == null) },
                enabled = isFinished,
                colors = ButtonDefaults.buttonColors(containerColor = if (isRewarded) Color(0xFF4CAF50) else Color(0xFF1E88E5))
            ) {
                Text(
                    text = when {
                        !isFinished -> "Aguarde..."
                        isRewarded && adLimitMessage != null -> "Limite Atingido 🚫"
                        isRewarded -> "Resgatar Moedas 🪙"
                        else -> "Continuar ➔"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isFinished) {
                TextButton(onClick = { onClose(false) }) {
                    Text("Fechar Anúncio", color = Color.Gray)
                }
            }
        }
    )
}



@Composable
fun GradesReportList(grades: List<NotaAluno>, viewModel: ProvalinoViewModel, prova: Prova) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "📊 Boletim e Notas da Prova", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E88E5))
        if (grades.isEmpty()) {
            ProvalinoEmptyState(
                title = "Nenhuma Nota Lançada",
                description = "Os resultados e notas dos alunos nesta avaliação aparecerão organizados aqui assim que forem lançados no sistema.",
                buttonText = null,
                onButtonClick = null
            )
        } else {
            grades.forEach { nota ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = nota.nomeAluno, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                            Text(text = "Nota: ${nota.nota} / 10.0", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4CAF50))
                            Text(text = "Data: ${nota.data}", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deleteNotaAluno(nota.id, prova.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir Nota", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateAppDialog(
    updateState: AppUpdateState,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!updateState.isForceUpdate) {
                onDismiss()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🚀", fontSize = 28.sp)
                Text(
                    text = updateState.updateTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = updateState.updateMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (updateState.releaseNotes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ Novidades da Versão:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateState.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Versão Instalada",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v${updateState.installedVersionCode}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Nova Versão na Play Store",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v${updateState.latestVersionCode}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_update_playstore")
            ) {
                Text("⚡ Atualizar na Google Play Store", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!updateState.isForceUpdate) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_dismiss_update")
                ) {
                    Text("Continuar sem atualizar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
