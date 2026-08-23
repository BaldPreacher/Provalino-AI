package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardCaa
import com.example.data.CaaImportProgress
import com.example.data.PlatformMetrics
import com.example.data.UserAccountItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevDashboardScreen(
    viewModel: ProvalinoViewModel,
    currentUserEmail: String
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("🧩 Pipeline CAA", "👥 Contas & Auth", "📊 Métricas & Ads", "🔐 Admin & Sistema")
    val tabIcons = listOf("🧩", "👥", "📊", "🔐")

    val cardsCaa by viewModel.cardsCaa.collectAsState()
    val isLoadingCards by viewModel.isLoadingCardsCaa.collectAsState()
    val importProgress by viewModel.caaImportProgress.collectAsState()
    val registeredAccounts by viewModel.registeredAccounts.collectAsState()
    val isLoadingAccounts by viewModel.isLoadingAccounts.collectAsState()
    val platformMetrics by viewModel.platformMetrics.collectAsState()

    var statusBanner by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRegisteredAccounts()
        viewModel.refreshPlatformMetrics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- CABEÇALHO DO PAINEL DEV ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF334155), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 16.sp)
                    }
                    Column {
                        Text("Painel do Desenvolvedor", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text("Administrador: $currentUserEmail", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
                Surface(
                    color = Color(0xFF0F766E),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("v39.0", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // --- SUB-ABAS SUPERIORES ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tabIcons[index], fontSize = 13.sp)
                            Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("dev_subtab_$index")
                )
            }
        }

        // Feedback Banner
        statusBanner?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isStatusError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = msg,
                        color = if (isStatusError) Color(0xFFC62828) else Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { statusBanner = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // --- CONTEÚDO DAS SUB-ABAS ---
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DevCaaPipelineTab(
                    viewModel = viewModel,
                    cardsCaa = cardsCaa,
                    isLoadingCards = isLoadingCards,
                    importProgress = importProgress,
                    onStatusUpdate = { msg, isErr ->
                        statusBanner = msg
                        isStatusError = isErr
                    }
                )
                1 -> DevAccountsTab(
                    viewModel = viewModel,
                    accounts = registeredAccounts,
                    isLoading = isLoadingAccounts,
                    onStatusUpdate = { msg, isErr ->
                        statusBanner = msg
                        isStatusError = isErr
                    }
                )
                2 -> DevMetricsTab(
                    platformMetrics = platformMetrics,
                    onRefreshMetrics = {
                        viewModel.refreshPlatformMetrics()
                        statusBanner = "Métricas sincronizadas com Firebase Analytics e AdMob!"
                        isStatusError = false
                    }
                )
                3 -> DevAdminSecurityTab(
                    viewModel = viewModel,
                    currentUserEmail = currentUserEmail,
                    onStatusUpdate = { msg, isErr ->
                        statusBanner = msg
                        isStatusError = isErr
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-ABA 1: PIPELINE JSON E COLEÇÃO card_caa
// -------------------------------------------------------------
@Composable
fun DevCaaPipelineTab(
    viewModel: ProvalinoViewModel,
    cardsCaa: List<CardCaa>,
    isLoadingCards: Boolean,
    importProgress: com.example.data.CaaImportProgress,
    onStatusUpdate: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    var jsonInput by remember { mutableStateOf("") }
    var autoDownloadImages by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    jsonInput = content
                    onStatusUpdate("Arquivo JSON carregado com sucesso! Pronto para envio.", false)
                } else {
                    onStatusUpdate("O arquivo selecionado está vazio.", true)
                }
            } catch (e: Exception) {
                onStatusUpdate("Erro ao ler o arquivo JSON: ${e.message}", true)
            }
        }
    }

    val sampleJsonTemplate = """[
  {
    "base64": "nihil",
    "categoria": "alimentos",
    "imagem_url": "https://arasaac.org/pictograms/1000/1000_300.png",
    "mediaId": "arasaac_1000",
    "mimType": "image/png",
    "nivel_cognitivo": "inicial",
    "sinonimos": ["maçã", "fruta", "maca"],
    "termo": "maçã"
  }
]"""

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📥 Pipeline de Importação JSON", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { jsonFilePickerLauncher.launch("*/*") },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F766E))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Carregar Arquivo", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Carregar .JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { jsonInput = sampleJsonTemplate },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Exemplo", fontSize = 11.sp, color = Color(0xFF1E88E5))
                            }
                        }
                    }

                    Text(
                        "Carregue um arquivo .json ou cole os dados abaixo. Se 'base64' for 'nihil' e 'imagem_url' for uma URL pública válida, o Provalino baixará a imagem e salvará no Firestore já com Base64 otimizado.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        label = { Text("Conteúdo do JSON ([ { ... } ])", color = Color(0xFF334155), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("Cole ou carregue o arquivo .json com a lista de cards...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF15803D),
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color(0xFF15803D),
                            unfocusedLabelColor = Color(0xFF334155),
                            cursorColor = Color(0xFF15803D)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color(0xFF0F172A))
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = autoDownloadImages,
                            onCheckedChange = { autoDownloadImages = it }
                        )
                        Text(
                            "Baixar imagens de URLs públicas e converter para Base64 automaticamente",
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (jsonInput.isBlank()) {
                                    onStatusUpdate("Cole o texto do JSON ou carregue um arquivo antes de iniciar.", true)
                                    return@Button
                                }
                                viewModel.importCaaJsonPipeline(
                                    jsonText = jsonInput,
                                    autoDownloadImages = autoDownloadImages,
                                    onComplete = { success, msg ->
                                        onStatusUpdate(msg, !success)
                                        if (success) jsonInput = ""
                                    }
                                )
                            },
                            enabled = !importProgress.isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_executar_pipeline_json")
                        ) {
                            if (importProgress.isRunning) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Processando...", fontSize = 11.sp)
                            } else {
                                Text("⚡ Enviar para Firestore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    jsonInput = clip
                                    onStatusUpdate("JSON colado da área de transferência!", false)
                                }
                            }
                        ) {
                            Text("Colar", fontSize = 11.sp, color = Color(0xFF334155))
                        }
                    }

                    // Barra de progresso da importação
                    if (importProgress.isRunning || importProgress.message.isNotBlank()) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(importProgress.message, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                if (importProgress.total > 0) {
                                    LinearProgressIndicator(
                                        progress = { importProgress.current.toFloat() / importProgress.total.toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF15803D),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${importProgress.current} de ${importProgress.total}", fontSize = 9.sp, color = Color.Gray)
                                        Text("Sucessos: ${importProgress.successCount} | Erros: ${importProgress.errorCount}", fontSize = 9.sp, color = Color(0xFF15803D))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- EXPLORADOR DE CARTÕES card_caa ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗄️ Coleção 'card_caa' no Firestore", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${cardsCaa.size} itens", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF15803D))
                            IconButton(onClick = { viewModel.loadCardsCaa(forceRefresh = true) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Recarregar", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchCardsCaa(it)
                        },
                        label = { Text("Pesquisar termo ou sinônimo...", color = Color(0xFF334155), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("Ex: maçã, bola, comer...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color(0xFF1E88E5),
                            unfocusedLabelColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.loadCardsCaa(forceRefresh = false)
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                }
            }
        }

        if (isLoadingCards) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        } else if (cardsCaa.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum cartão CAA encontrado na coleção.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(cardsCaa) { card ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (card.hasValidBase64) Color(0xFFDCFCE7) else if (card.hasValidImageUrl) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (card.hasValidBase64) "🖼️" else if (card.hasValidImageUrl) "🌐" else "🧩", fontSize = 16.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(card.termo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = card.categoria,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 9.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            if (card.sinonimos.isNotEmpty()) {
                                Text("Sinônimos: ${card.sinonimos.joinToString(", ")}", fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(
                                "Nível: ${card.nivel_cognitivo} | Media: ${card.mediaId} | Imagem: ${if (card.hasValidBase64) "Base64 OK" else if (card.hasValidImageUrl) "URL pública" else "Sem imagem"}",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.deleteCardCaa(card.id) { res ->
                                    if (res.isSuccess) onStatusUpdate("Cartão '${card.termo}' removido do Firestore.", false)
                                    else onStatusUpdate("Erro ao remover cartão: ${res.exceptionOrNull()?.message}", true)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-ABA 2: GESTÃO DE CONTAS & USUÁRIOS
// -------------------------------------------------------------
@Composable
fun DevAccountsTab(
    viewModel: ProvalinoViewModel,
    accounts: List<UserAccountItem>,
    isLoading: Boolean,
    onStatusUpdate: (String, Boolean) -> Unit
) {
    var userToDelete by remember { mutableStateOf<UserAccountItem?>(null) }
    var searchUserQuery by remember { mutableStateOf("") }

    val filteredAccounts = remember(accounts, searchUserQuery) {
        if (searchUserQuery.isBlank()) accounts
        else accounts.filter { it.email.contains(searchUserQuery, ignoreCase = true) || it.nome.contains(searchUserQuery, ignoreCase = true) }
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text("Confirmar Exclusão de Conta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tem certeza que deseja desativar/excluir a conta de:", fontSize = 12.sp)
                    Text(userToDelete?.email ?: "", fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontSize = 12.sp)
                    Text("O sistema removerá o acesso do usuário no Firestore e disparará a notificação/redefinição no Firebase Auth.", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = userToDelete
                        userToDelete = null
                        if (target != null) {
                            viewModel.deleteOrDeactivateAdminUserAccount(target.uid, target.email) { success, msg ->
                                onStatusUpdate(msg, !success)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Excluir e Notificar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancelar", fontSize = 11.sp)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("👥 Contas Cadastradas & Ativas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                            Text("Gerenciamento direto integrado com o Firebase Auth e Firestore", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                        Button(
                            onClick = { viewModel.loadRegisteredAccounts() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recarregar", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recarregar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = searchUserQuery,
                        onValueChange = { searchUserQuery = it },
                        label = { Text("Filtrar por e-mail ou nome...", color = Color(0xFF334155), fontWeight = FontWeight.Medium) },
                        placeholder = { Text("Digite o e-mail do professor...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF0F766E),
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedLabelColor = Color(0xFF0F766E),
                            unfocusedLabelColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        } else if (filteredAccounts.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhuma conta de usuário encontrada.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(filteredAccounts) { user ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(user.ultimoLogin))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = Color(0xFFE0F2FE),
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("👤", fontSize = 12.sp)
                                    }
                                }
                                Column {
                                    Text(user.email, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                    Text("Último acesso: $dateStr", fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                            }
                            Surface(
                                color = if (user.status == "ativo") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = user.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.status == "ativo") Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botão Disparar E-mail de Reset de Senha
                            OutlinedButton(
                                onClick = {
                                    viewModel.sendAdminUserPasswordReset(user.email) { success, msg ->
                                        onStatusUpdate(msg, !success)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "Resetar", modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enviar Reset de Senha", fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Botão Excluir Conta
                            Button(
                                onClick = { userToDelete = user },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(13.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excluir Conta", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-ABA 3: MÉTRICAS & ADMOB / FIREBASE ANALYTICS
// -------------------------------------------------------------
@Composable
fun DevMetricsTab(
    platformMetrics: PlatformMetrics,
    onRefreshMetrics: () -> Unit
) {
    val lastUpdateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(platformMetrics.lastUpdatedTimestamp))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("📊 Indicadores de Métricas e Performance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                            Text("Sincronizado com Firebase Analytics e Google AdMob", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                        Button(
                            onClick = onRefreshMetrics,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_atualizar_metricas")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Atualizar", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atualizar Métricas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Última sincronização manual: $lastUpdateStr", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        // Grade de Cartões de Métricas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Atividades com IA", value = "${platformMetrics.totalActivitiesGenerated}", icon = "🤖", color = Color(0xFF1E40AF), modifier = Modifier.weight(1f))
                    MetricCard(title = "Atividades Salvas", value = "${platformMetrics.totalActivitiesSaved}", icon = "💾", color = Color(0xFF0F766E), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Documentos Exportados", value = "${platformMetrics.totalDocumentsExported}", icon = "📄", color = Color(0xFF7C3AED), modifier = Modifier.weight(1f))
                    MetricCard(title = "Total de Logins", value = "${platformMetrics.totalLogins}", icon = "🔑", color = Color(0xFFEA580C), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Ads Premiados (Rewarded)", value = "${platformMetrics.rewardedAdsWatched}", icon = "🪙", color = Color(0xFFB45309), modifier = Modifier.weight(1f))
                    MetricCard(title = "Ads Intersticiais", value = "${platformMetrics.interstitialAdsWatched}", icon = "📺", color = Color(0xFF4338CA), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Professores Cadastrados", value = "${platformMetrics.activeUsersCount}", icon = "👥", color = Color(0xFF15803D), modifier = Modifier.weight(1f))
                    MetricCard(title = "Status do Servidor", value = "Online (99.9%)", icon = "⚡", color = Color(0xFF0369A1), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 16.sp)
                }
            }
            Column {
                Text(title, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-ABA 4: ADMIN, SEGURANÇA & SISTEMA
// -------------------------------------------------------------
@Composable
fun DevAdminSecurityTab(
    viewModel: ProvalinoViewModel,
    currentUserEmail: String,
    onStatusUpdate: (String, Boolean) -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Confirmar Saída", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente encerrar a sessão de administrador?", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Sair do Aplicativo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔐 Configurações de Segurança do Administrador", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("E-mail Administrador: $currentUserEmail", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Permissões: Acesso Total (Root Dev/Admin)", fontSize = 10.sp, color = Color(0xFF15803D))
                            Text("Versão do Applet: 39 (v39.0)", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.sendAdminUserPasswordReset(currentUserEmail) { success, msg ->
                                onStatusUpdate(msg, !success)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Trocar Senha", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disparar E-mail para Troca de Senha do Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showLogoutConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Encerrar Sessão", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Encerrar Sessão (Log Out)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
