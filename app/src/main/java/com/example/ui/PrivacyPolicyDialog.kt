package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
    onAccept: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text("🦉", fontSize = 28.sp)
                    Column {
                        Text(
                            text = "Política de Privacidade",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Provalino AI — Proteção de Dados (LGPD)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable Content
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PolicySectionTitle("1. Apresentação e Compromisso LGPD")
                    PolicyBodyText(
                        "O Provalino AI é um aplicativo educacional desenvolvido para auxiliar professores e equipes pedagógicas na criação, organização e adaptação de atividades escolares acessíveis (DUA/AEE). Assumimos o compromisso rigoroso de proteger a privacidade dos usuários e a confidencialidade dos dados escolares em conformidade com a Lei Geral de Proteção de Dados Pessoais (LGPD - Lei nº 13.709/2018)."
                    )

                    PolicySectionTitle("2. Dados Coletados e Finalidade")
                    PolicyBodyText(
                        "• Dados de Cadastro e Autenticação: Para acessar a plataforma, coletamos seu nome e e-mail via Google Sign-In ou Firebase Authentication. Esses dados identificam o professor responsável pela conta.\n" +
                        "• Informações Pedagógicas e Turmas: Dados de turmas, perfis de adaptação (como DUA/AEE) e questões pedagógicas criadas são armazenados no banco de dados para sincronização da sua biblioteca de materiais.\n" +
                        "• Inteligência Artificial (Gemini API): O processamento com IA é utilizado exclusivamente para adaptar enunciados, sugerir recursos pedagógicos e simplificar linguagens conforme o perfil selecionado pelo docente. Não são enviados nem processados dados pessoais identificáveis de estudantes em modelos de treino público."
                    )

                    PolicySectionTitle("3. Não Comercialização e Compartilhamento")
                    PolicyBodyText(
                        "Nenhuma informação pessoal de professores, alunos ou relatórios de adaptação é vendida, alugada ou compartilhada com terceiros para fins de publicidade ou marketing. Os dados são mantidos restritos ao uso exclusivo dentro do ambiente do Provalino AI."
                    )

                    PolicySectionTitle("4. Armazenamento e Segurança dos Dados")
                    PolicyBodyText(
                        "Adotamos medidas técnicas e administrativas aptas a proteger os dados pessoais contra acessos não autorizados e situações acidentais ou ilícitas. A comunicação com os servidores do Firebase e serviços em nuvem é protegida com criptografia SSL/TLS e regras estritas de acesso."
                    )

                    PolicySectionTitle("5. Direitos do Usuário e Exclusão de Dados")
                    PolicyBodyText(
                        "Nos termos da LGPD, você tem o direito de acessar, corrigir, atualizar ou solicitar a exclusão definitiva da sua conta e de todos os dados a ela vinculados a qualquer momento. Caso deseje encerrar sua conta e remover seus dados armazenados, você pode solicitar diretamente pelo app ou enviando um e-mail para marcio.moura2708@gmail.com."
                    )

                    PolicySectionTitle("6. Alterações nesta Política")
                    PolicyBodyText(
                        "Esta Política de Privacidade poderá ser atualizada periodicamente para refletir melhorias no aplicativo ou mudanças regulatórias. Recomendamos a consulta periódica deste documento."
                    )

                    PolicySectionTitle("7. Contato do Encarregado (DPO)")
                    PolicyBodyText(
                        "Para dúvidas, solicitações de exclusão de dados ou esclarecimentos sobre nossa política de privacidade, entre em contato através do e-mail: marcio.moura2708@gmail.com."
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (onAccept != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onAccept()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Li e Concordo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun PolicyBodyText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 17.sp
    )
}
