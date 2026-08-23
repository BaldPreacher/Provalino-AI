package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PictogramBadge
import com.example.data.PictogramInjector
import android.graphics.BitmapFactory
import android.util.Base64

/**
 * Componente visual Compose que renderiza os cartões de Pictogramas DUA / CAA
 * (Comunicação Aumentativa e Alternativa) estruturados em formato de prancha/cards
 * de alta legibilidade, com contraste e dimensões pedagógicas ideais.
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
                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🧩 CARTÕES VISUAIS DE APOIO (CAA):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F766E)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = Color(0xFF0F766E)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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

    // Decodifica imagem Base64 se presente no modelo card_caa
    val base64Bitmap = remember(badge.base64Image) {
        badge.base64Image?.let { rawB64 ->
            try {
                val clean = if (rawB64.contains("base64,")) rawB64.substringAfter("base64,").trim() else rawB64.trim()
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        modifier = modifier
            .widthIn(min = 76.dp, max = 104.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(2.dp, if (badge.isCardCaa) Color(0xFF0284C7) else Color(0xFF0F766E), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (badge.isCardCaa) Color(0xFFF0F9FF) else Color(0xFFF0FDFA), RoundedCornerShape(6.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                base64Bitmap != null -> {
                    Image(
                        bitmap = base64Bitmap,
                        contentDescription = badge.termo,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                !badge.imageUrl.isNullOrBlank() && (badge.imageUrl.startsWith("http://") || badge.imageUrl.startsWith("https://")) -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(badge.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = badge.termo,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                badge.isArasaac && !badge.arasaacUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(badge.arasaacUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = badge.termo,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    Text(
                        text = badge.simboloLocal.ifBlank { "🖼️" },
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = badge.termo,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
