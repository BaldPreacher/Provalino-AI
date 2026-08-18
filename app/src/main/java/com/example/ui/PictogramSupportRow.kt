package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PictogramBadge
import com.example.data.PictogramInjector

/**
 * Componente visual Compose que renderiza os cartões de Pictogramas DUA / CAA
 * (Comunicação Aumentativa e Alternativa). Apresenta símbolo/imagem oficial e legenda
 * em caixa alta no padrão pedagógico para apoio à alfabetização e inclusão.
 */
@Composable
fun PictogramSupportRow(
    rawPictogramText: String,
    modifier: Modifier = Modifier
) {
    if (rawPictogramText.isBlank()) return

    val terms = remember(rawPictogramText) { PictogramInjector.extractTerms(rawPictogramText) }
    var badges by remember(rawPictogramText) { mutableStateOf<List<PictogramBadge>>(emptyList()) }
    var isLoading by remember(rawPictogramText) { mutableStateOf(true) }

    LaunchedEffect(rawPictogramText) {
        isLoading = true
        badges = PictogramInjector.resolveBadges(terms)
        isLoading = false
    }

    if (badges.isNotEmpty() || isLoading) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🎨 SUPORTE VISUAL (CAA / DUA):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = Color(0xFF166534)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                badges.forEach { badge ->
                    PictogramCardItem(badge = badge)
                }
            }
        }
    }
}

@Composable
fun PictogramCardItem(
    badge: PictogramBadge,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .widthIn(min = 60.dp, max = 85.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.5.dp, Color(0xFF15803D), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (badge.isArasaac && !badge.arasaacUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(badge.arasaacUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = badge.termo,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = badge.simboloLocal.ifBlank { "🖼️" },
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = badge.termo,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
