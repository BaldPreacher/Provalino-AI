package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSession

/**
 * Modal flutuante com o perfil do professor, permitindo:
 * - Edição de nome de usuário (user_name) salvo no Firestore (padrão: início do e-mail)
 * - Inclusão de Escola Predefinida (escola_padrao) salva no Firestore (ou em branco se não informada)
 * - Solicitação de alteração de senha
 * - Conformidade LGPD & Termos
 * - Sair da conta
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileBottomSheet(
    currentUser: UserSession?,
    teacherProfileData: Map<String, String>,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onSaveProfile: (String, String, (Boolean, String) -> Unit) -> Unit,
    onRequestPasswordReset: (String, (Boolean, String) -> Unit) -> Unit
) {
    val email = currentUser?.email ?: "docente@escola.gov.br"
    val defaultUserNameFromEmail = email.substringBefore("@")

    val initialUserName = remember(teacherProfileData, currentUser) {
        teacherProfileData["user_name"]?.takeIf { it.isNotBlank() }
            ?: currentUser?.userName?.takeIf { it.isNotBlank() }
            ?: defaultUserNameFromEmail
    }

    val initialEscola = remember(teacherProfileData, currentUser) {
        teacherProfileData["escola_padrao"]
            ?: currentUser?.escolaPadrao
            ?: ""
    }

    var userNameInput by remember(initialUserName) { mutableStateOf(initialUserName) }
    var escolaPadraoInput by remember(initialEscola) { mutableStateOf(initialEscola) }

    var isSavingProfile by remember { mutableStateOf(false) }
    var profileSaveFeedback by remember { mutableStateOf<String?>(null) }
    var isProfileSaveSuccess by remember { mutableStateOf(true) }

    var isResettingPassword by remember { mutableStateOf(false) }
    var resetFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackSuccess by remember { mutableStateOf(true) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val displayInitials = (userNameInput.ifBlank { defaultUserNameFromEmail }).take(1).uppercase()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 42.dp, height = 4.dp),
                color = Color(0xFFCBD5E1),
                shape = RoundedCornerShape(2.dp)
            ) {}
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Foto/Avatar e Dados Principais
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E88E5),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayInitials,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prof(a). ${userNameInput.ifBlank { defaultUserNameFromEmail }}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = email,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = "Docente Cadastrado(a)",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar Perfil",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.5.dp)

            // Seção de Edição: Nome de Usuário e Escola Padrão
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚙️", fontSize = 14.sp)
                        Text(
                            text = "Configurações do Docente",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Campo: Nome do Docente
                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = {
                            userNameInput = it
                            profileSaveFeedback = null
                        },
                        label = { Text("Nome do Docente", fontSize = 11.sp) },
                        placeholder = { Text("Ex: ${defaultUserNameFromEmail}", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_username"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Campo: Escola Predefinida (escola_padrao)
                    OutlinedTextField(
                        value = escolaPadraoInput,
                        onValueChange = {
                            escolaPadraoInput = it
                            profileSaveFeedback = null
                        },
                        label = { Text("Escola Predefinida (opcional)", fontSize = 11.sp) },
                        placeholder = { Text("Deixe em branco se preferir", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_escola"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Feedback de Salvamento do Perfil
                    if (profileSaveFeedback != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isProfileSaveSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (isProfileSaveSuccess) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(if (isProfileSaveSuccess) "✅" else "⚠️", fontSize = 12.sp)
                                Text(
                                    text = profileSaveFeedback!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isProfileSaveSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    // Botão Salvar Dados do Perfil
                    Button(
                        onClick = {
                            val finalUserName = userNameInput.trim().ifBlank { defaultUserNameFromEmail }
                            val finalEscola = escolaPadraoInput.trim()
                            isSavingProfile = true
                            profileSaveFeedback = null
                            onSaveProfile(finalUserName, finalEscola) { success, msg ->
                                isSavingProfile = false
                                isProfileSaveSuccess = success
                                profileSaveFeedback = msg
                            }
                        },
                        enabled = !isSavingProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_save_teacher_profile"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        )
                    ) {
                        if (isSavingProfile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Salvar Alterações do Perfil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Mensagem de Feedback de Senha (se houver)
            if (resetFeedbackMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFeedbackSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isFeedbackSuccess) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(if (isFeedbackSuccess) "✅" else "⚠️", fontSize = 16.sp)
                        Text(
                            text = resetFeedbackMessage!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isFeedbackSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                    }
                }
            }

            // Opção: Solicitar Redefinição de Senha
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isResettingPassword) {
                        isResettingPassword = true
                        resetFeedbackMessage = null
                        onRequestPasswordReset(email) { success, msg ->
                            isResettingPassword = false
                            isFeedbackSuccess = success
                            resetFeedbackMessage = msg
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alterar / Redefinir Senha",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Envia link seguro para seu e-mail cadastrado",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (isResettingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2563EB)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Opção: Privacidade & LGPD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onOpenPrivacyPolicy()
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛡️", fontSize = 18.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Termos & Proteção de Dados (LGPD)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Anonimização de alunos e criptografia em conformidade com o MEC",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Opção: Sair da Conta (Logout)
            Button(
                onClick = { showLogoutConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_profile_sign_out"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = Color(0xFFDC2626)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sair da Conta",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sair da Conta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🚪", fontSize = 20.sp)
                    Text("Deseja realmente sair?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Você precisará fazer login novamente para acessar seus alunos e atividades salvas.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onDismiss()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Sim, Sair", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
