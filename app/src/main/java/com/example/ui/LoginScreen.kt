package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.data.AnalyticsRepository
import com.example.data.DevLogger
import com.example.data.DevErrorLog
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import com.aistudio.provalino.teacher.abcxyz.R

private fun mapGoogleAuthErrorToUserMessage(e: Exception): String {
    val className = e.javaClass.name
    val message = e.message ?: ""
    val localized = e.localizedMessage ?: ""

    return when {
        className.contains("GetCredentialCancellationException") ||
        className.contains("UserCanceled") ||
        message.contains("canceled", ignoreCase = true) ||
        message.contains("cancelled", ignoreCase = true) -> {
            "Autenticação cancelada. Para acessar com o Google, selecione sua conta no menu."
        }
        className.contains("NoCredentialException") ||
        message.contains("No credential", ignoreCase = true) ||
        message.contains("no eligible accounts", ignoreCase = true) -> {
            "Nenhuma conta Google ativa foi encontrada no dispositivo/emulador. Adicione uma conta nas configurações do Android ou entre com seu e-mail e senha cadastrados acima."
        }
        className.contains("DeveloperError") ||
        message.contains("10: Developer error", ignoreCase = true) ||
        message.contains("DEVELOPER_ERROR", ignoreCase = true) ||
        message.contains("API key not valid", ignoreCase = true) ||
        message.contains("Identity Toolkit", ignoreCase = true) ||
        message.contains("caller is not allowed", ignoreCase = true) -> {
            "Configuração do Google Cloud / Firebase necessária: habilite a API 'Identity Toolkit' e cadastre a chave SHA-1 no console do Firebase."
        }
        message.contains("network", ignoreCase = true) ||
        message.contains("connection", ignoreCase = true) ||
        message.contains("Timeout", ignoreCase = true) -> {
            "Sem conexão com os serviços do Google. Verifique sua conexão com a internet e tente novamente."
        }
        else -> {
            if (localized.isNotBlank() && localized.length < 120) {
                "Aviso do Google Sign-In: $localized. Utilize seu e-mail e senha para entrar."
            } else {
                "Não foi possível autenticar com o Google no momento. Entre utilizando seu e-mail e senha acima."
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: ProvalinoViewModel,
    onGoogleSignInClicked: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var agreedToLgpd by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showLgpdDetailsDialog by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showDevLogsDialog by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSendingReset by remember { mutableStateOf(false) }

    val devLogs by DevLogger.logs.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val handleGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            Log.d("LoginScreen", "=== INICIANDO FLUXO DE LOGIN COM GOOGLE ===")
            isGoogleLoading = true
            localError = null
            try {
                if (!agreedToLgpd) {
                    localError = "Você deve concordar com os Termos de Segurança e LGPD para continuar."
                    Log.w("LoginScreen", "Tentativa de login com Google sem aceite LGPD.")
                    AnalyticsRepository.logLoginError("google", "lgpd_not_agreed")
                    DevLogger.logError(context, "GOOGLE_AUTH", "Tentativa de login sem aceite da LGPD.")
                    return@launch
                }
                val credentialManager = CredentialManager.create(context)
                Log.d("LoginScreen", "CredentialManager criado com sucesso.")
                
                // Limpa estado de credencial em cache para forçar a seleção de conta
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    Log.d("LoginScreen", "Estado de credencial limpo com sucesso.")
                } catch (ce: Exception) {
                    Log.w("LoginScreen", "Aviso ao limpar estado de credencial: ${ce.message}")
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Force account selection, clear pre-selected/cached accounts
                    .setAutoSelectEnabled(false)
                    .setServerClientId("12454269674-ismit34vnk620mg9msg2hev3cd2kct22.apps.googleusercontent.com")
                    .build()
                Log.d("LoginScreen", "GetGoogleIdOption configurado com ServerClientId.")

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.d("LoginScreen", "Chamando credentialManager.getCredential...")
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                Log.d("LoginScreen", "Credencial obtida com sucesso. Tipo: ${credential.type}")

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    Log.d("LoginScreen", "GoogleIdTokenCredential decodificado com sucesso. Token obtido.")
                    viewModel.signInWithGoogle(idToken)
                } else {
                    val err = "Tipo de credencial inválido recebido: ${credential.type}"
                    Log.e("LoginScreen", err)
                    localError = "Tipo de credencial inválido recebido do Google."
                    AnalyticsRepository.logLoginError("google", err)
                    DevLogger.logError(context, "GOOGLE_AUTH", err)
                }
            } catch (e: Exception) {
                val userMsg = mapGoogleAuthErrorToUserMessage(e)
                val rawLog = "${e.javaClass.simpleName}: ${e.localizedMessage ?: e.message}"
                val isExpectedFlow = e.javaClass.name.contains("NoCredentialException") ||
                                    e.javaClass.name.contains("GetCredentialCancellationException") ||
                                    rawLog.contains("canceled", ignoreCase = true) ||
                                    rawLog.contains("cancelled", ignoreCase = true)

                if (isExpectedFlow) {
                    Log.i("LoginScreen", "Google Sign-In fluxo sem credencial ou cancelado: $rawLog")
                } else {
                    Log.e("LoginScreen", "Exceção no Google Sign-In: $rawLog", e)
                    AnalyticsRepository.logLoginError("google", rawLog)
                    DevLogger.logError(context, "GOOGLE_AUTH", rawLog, e)
                }
                localError = userMsg
            } finally {
                isGoogleLoading = false
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.ime
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mascote Provalino Centralizado
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.provalino_mascot),
                        contentDescription = "Coruja Mestre Provalino",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Provalino AI",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Assistente Pedagógico Inclusivo",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isRegisterMode) "🚀 Crie sua conta para adaptar provas em segundos!" else "👋 Olá, Professor(a)! Entre para acessar suas turmas.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                if (!isRegisterMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                forgotPasswordEmail = email.trim()
                                resetSuccessMessage = null
                                resetErrorMessage = null
                                showForgotPasswordDialog = true
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "🔑 Esqueceu sua senha?",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Checkbox LGPD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Checkbox(
                        checked = agreedToLgpd,
                        onCheckedChange = { agreedToLgpd = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Concordo com os Termos de Proteção de Dados (LGPD).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                        TextButton(
                            onClick = { showLgpdDetailsDialog = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "📄 Ler Termos de Privacidade",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val displayError = localError ?: authError
                if (!displayError.isNullOrEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!agreedToLgpd) {
                            localError = "Você deve concordar com os Termos e LGPD para continuar."
                            return@Button
                        }
                        localError = null
                        if (isRegisterMode) {
                            viewModel.signUpWithEmail(email, password)
                        } else {
                            viewModel.signInWithEmail(email, password)
                        }
                    },
                    enabled = !authLoading && !isGoogleLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (authLoading && !isGoogleLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isRegisterMode) "Criar Minha Conta" else "Entrar com E-mail",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (!agreedToLgpd) {
                            localError = "Você deve concordar com os Termos e LGPD para continuar."
                            return@OutlinedButton
                        }
                        localError = null
                        handleGoogleSignIn()
                    },
                    enabled = !authLoading && !isGoogleLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    if (isGoogleLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Conectando ao Google...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text(
                            "🔍 Entrar com Conta Google",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                TextButton(
                    onClick = { isRegisterMode = !isRegisterMode }
                ) {
                    Text(
                        text = if (isRegisterMode) "Já possui uma conta? Entre aqui" else "Não tem conta? Cadastre-se aqui",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                TextButton(
                    onClick = { showDevLogsDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "🛠️ Logs de Diagnóstico do Dev (${devLogs.size})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSendingReset) {
                    showForgotPasswordDialog = false
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔑 Recuperar Senha", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Digite o e-mail associado à sua conta do Provalino. Enviaremos um link seguro para você redefinir sua senha.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("E-mail cadastrado") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (!resetErrorMessage.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resetErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (!resetSuccessMessage.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resetSuccessMessage ?: "",
                                color = Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        resetErrorMessage = null
                        resetSuccessMessage = null
                        isSendingReset = true
                        viewModel.sendPasswordResetEmail(forgotPasswordEmail) { success, msg ->
                            isSendingReset = false
                            if (success) {
                                resetSuccessMessage = msg
                            } else {
                                resetErrorMessage = msg
                            }
                        }
                    },
                    enabled = !isSendingReset && resetSuccessMessage == null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSendingReset) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(if (resetSuccessMessage != null) "Enviado!" else "Enviar Link")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false },
                    enabled = !isSendingReset
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showLgpdDetailsDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showLgpdDetailsDialog = false },
            onAccept = {
                agreedToLgpd = true
                showLgpdDetailsDialog = false
            }
        )
    }

    if (showDevLogsDialog) {
        DevLogsDialog(
            logs = devLogs,
            onDismiss = { showDevLogsDialog = false },
            onClear = { DevLogger.clearLogs(context) }
        )
    }
}

@Composable
fun DevLogsDialog(
    logs: List<DevErrorLog>,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🛠️ Logs de Diagnóstico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onClear) {
                    Text("Limpar", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        "Nenhum erro registrado até o momento.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    logs.forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "📌 [${log.category}]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        log.timestamp,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    log.message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (log.stackTrace.isNotBlank()) {
                                    Text(
                                        log.stackTrace,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 6,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
