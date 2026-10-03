package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimeirosPassosScreen(
    onVoltar: () -> Unit,
    onCadastrarAlunoClick: () -> Unit,
    onGerarProvaClick: () -> Unit,
    onVerProvasClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🚀", fontSize = 22.sp)
                        Text("Primeiros Passos", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVoltar,
                        modifier = Modifier.testTag("voltar_primeiros_passos_button")
                    ) {
                        Text("⬅️", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC),
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👋 Bem-vindo(a) ao Provalino!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "Sua plataforma inteligente para criação de avaliações e atividades adaptadas para a Educação Inclusiva (AEE / DUA). Siga os 3 passos simples abaixo:",
                        color = Color(0xFFE0F2FE),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // PASSO 1: Cadastrar Aluno
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("passo_1_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            "Cadastrar Aluno com Necessidades Específicas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )
                        Text("🧑‍🎓", fontSize = 24.sp)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text(
                        "Acesse a aba 'Inclusão / Alunos' e cadastre a carteira do seu aluno informando:",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StepItemText("• Nome do Aluno e Ano Escolar/Etapa")
                        StepItemText("• Diagnóstico/Laudo ou Perfil (Autismo, TDAH, Dislexia, Deficiência Intelectual, Down, etc.)")
                        StepItemText("• Nível Cognitivo e Preferências de Apoio Visual (Cartões CAA ou Atividade para Colorir)")
                    }

                    Button(
                        onClick = onCadastrarAlunoClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_cadastrar_aluno_button")
                    ) {
                        Text("➕ Cadastrar Primeiro Aluno Agora", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // PASSO 2: Criar Primeira Atividade Adaptada
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("passo_2_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF059669), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            "Gerar Atividade Adaptada com IA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )
                        Text("✨", fontSize = 24.sp)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text(
                        "No criador de atividades com Inteligência Artificial, defina o tema e a IA adaptará tudo automaticamente:",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StepItemText("• Digite o Assunto ou Código BNCC (Ex: 'Brasil Colônia', 'Frações', 'Sistema Solar')")
                        StepItemText("• Selecione o Aluno da sua carteira para aplicar o laudo de adaptação automaticamente")
                        StepItemText("• A IA gerará enunciados acessíveis, questões de ligar, múltipla escolha e cartões de suporte CAA")
                    }

                    Button(
                        onClick = onGerarProvaClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_gerar_prova_button")
                    ) {
                        Text("✨ Criar Primeira Atividade Adaptada", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // PASSO 3: Impressão e Compartilhamento
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("passo_3_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF7C3AED), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            "Imprimir ou Exportar em PDF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )
                        Text("🖨️", fontSize = 24.sp)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text(
                        "Após gerar sua avaliação, você poderá:",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StepItemText("• 🖨️ Imprimir diretamente do celular no formato A4")
                        StepItemText("• 📤 Compartilhar em PDF vetorial via WhatsApp ou E-mail com qualidade de gráfica")
                        StepItemText("• 📋 Copiar o texto formatado para edição externa")
                    }

                    OutlinedButton(
                        onClick = onVerProvasClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_ver_provas_button")
                    ) {
                        Text("📑 Ver Minhas Atividades Criadas", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepItemText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF475569)
    )
}
