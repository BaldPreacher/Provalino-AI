package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Paleta de Cores e Estilo Visual Claymation (Massinha de Modelar / Stop-Motion Tátil)
 */
object ClayColors {
    // Fundo geral e superfícies táteis
    val BackgroundMint = Color(0xFFE2F3F0)
    val BackgroundSoft = Color(0xFFF1F8F6)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val SurfaceCream = Color(0xFFFDFBF7)

    // Cores Principais de Massinha
    val Teal = Color(0xFF0D9488)
    val TealLight = Color(0xFF2DD4BF)
    val TealDark = Color(0xFF115E59)
    val TealShadow = Color(0xFF0F766E)

    val Green = Color(0xFF22C55E)
    val GreenLight = Color(0xFF4ADE80)
    val GreenDark = Color(0xFF15803D)
    val GreenShadow = Color(0xFF166534)

    val Blue = Color(0xFF3B82F6)
    val BlueLight = Color(0xFF60A5FA)
    val BlueDark = Color(0xFF1D4ED8)
    val BlueShadow = Color(0xFF1E40AF)

    val Orange = Color(0xFFF97316)
    val OrangeLight = Color(0xFFFB923C)
    val OrangeDark = Color(0xFFC2410C)
    val OrangeShadow = Color(0xFF9A3412)

    val Gold = Color(0xFFFBBF24)
    val GoldLight = Color(0xFFFDE047)
    val GoldDark = Color(0xFFD97706)
    val GoldShadow = Color(0xFFB45309)

    val Purple = Color(0xFFA855F7)
    val PurpleLight = Color(0xFFC084FC)
    val PurpleDark = Color(0xFF7E22CE)
    val PurpleShadow = Color(0xFF6B21A8)

    val Grey = Color(0xFF64748B)
    val GreyLight = Color(0xFF94A3B8)
    val GreyDark = Color(0xFF334155)
    val GreyShadow = Color(0xFF1E293B)

    val Red = Color(0xFFEF4444)
    val RedLight = Color(0xFFF87171)
    val RedDark = Color(0xFFB91C1C)
    val RedShadow = Color(0xFF991B1B)

    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF334155)
    val TextMuted = Color(0xFF64748B)
}

/**
 * Modificador com escala elástica tátil (Spring Bounce ao tocar).
 */
@Composable
fun Modifier.clayBounce(
    enabled: Boolean = true,
    scaleDown: Float = 0.94f,
    onClick: () -> Unit = {}
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "clay_bounce_scale"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isPressed && enabled) 3f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "clay_bounce_offset"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationY = offsetY
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

@Composable
fun Modifier.clayScale(scaleFactor: Float = 1.0f): Modifier {
    return this.graphicsLayer {
        scaleX = scaleFactor
        scaleY = scaleFactor
    }
}

/**
 * Botão tátil em estilo Claymation (com relevo 3D de massinha, sombras e animação física).
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClayColors.Teal,
    darkShadowColor: Color = ClayColors.TealDark,
    lightHighlightColor: Color = Color.White.copy(alpha = 0.35f),
    contentColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "clay_btn_scale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed && enabled) 4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "clay_btn_offset"
    )

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
            .shadow(
                elevation = if (isPressed) 1.dp else elevation,
                shape = shape,
                clip = false,
                ambientColor = darkShadowColor.copy(alpha = 0.5f),
                spotColor = darkShadowColor
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.95f),
                        backgroundColor,
                        darkShadowColor
                    )
                )
            )
            .border(
                BorderStroke(
                    2.dp,
                    Brush.verticalGradient(
                        colors = listOf(lightHighlightColor, darkShadowColor.copy(alpha = 0.8f))
                    )
                ),
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

/**
 * Card tátil em estilo Claymation (efeito de placa de massinha com cantos arredondados e borda volumosa).
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClayColors.SurfaceWhite,
    borderColor: Color = ClayColors.BackgroundMint,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = Color(0x22000000),
                spotColor = Color(0x33000000)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    2.5.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color.White, borderColor)
                    )
                ),
                shape
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

/**
 * Botão / Aba Tátil estilo Claymation (como os botões "Visualização & PDF", "Dar Notas", "Boletim" dos protótipos).
 */
@Composable
fun ClayTabButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Any? = null, // Can be ImageVector or String (emoji)
    badgeText: String? = null,
    selected: Boolean = false,
    isSelected: Boolean = selected,
    backgroundColor: Color = ClayColors.Teal,
    darkShadowColor: Color = ClayColors.TealDark,
    selectedColor: Color = backgroundColor,
    selectedDarkShadow: Color = darkShadowColor,
    unselectedColor: Color = ClayColors.GreyLight,
    unselectedDarkShadow: Color = ClayColors.GreyDark,
    textColor: Color = Color.White,
    testTag: String? = null
) {
    val activeSelected = isSelected || selected
    val activeBg = if (activeSelected) selectedColor else unselectedColor
    val activeShadow = if (activeSelected) selectedDarkShadow else unselectedDarkShadow

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "clay_tab_scale"
    )

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (activeSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = activeShadow.copy(alpha = 0.6f),
                spotColor = activeShadow
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (activeSelected) {
                        listOf(activeBg, activeShadow)
                    } else {
                        listOf(activeBg.copy(alpha = 0.85f), activeShadow.copy(alpha = 0.85f))
                    }
                )
            )
            .border(
                BorderStroke(
                    if (activeSelected) 2.5.dp else 1.5.dp,
                    if (activeSelected) Color.White else Color.White.copy(alpha = 0.5f)
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
                is String -> {
                    Text(
                        text = icon,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            if (!badgeText.isNullOrBlank()) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Badge de Moedas tátil estilo Massinha Dourada.
 */
@Composable
fun ClayCoinBadge(
    coins: Int,
    onClick: () -> Unit = {},
    onAddCoinsClick: () -> Unit = onClick,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "clay_coin_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ClayColors.GoldDark.copy(alpha = 0.5f),
                spotColor = ClayColors.GoldDark
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ClayColors.GoldLight, ClayColors.Gold, ClayColors.GoldDark)
                )
            )
            .border(
                BorderStroke(2.dp, Color.White),
                RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onAddCoinsClick
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🪙", fontSize = 13.sp)
            Text(
                text = "$coins Moedas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF78350F)
            )
            Box(
                modifier = Modifier
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF15803D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ganhar moedas",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Campo de texto em estilo Claymation (com borda esculpida e alta legibilidade).
 */
@Composable
fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    accentColor: Color = ClayColors.Teal,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ClayColors.TextSecondary
            )
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = ClayColors.TextMuted
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .background(Color.White, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ClayColors.TextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ClayColors.TextPrimary,
            unfocusedTextColor = ClayColors.TextPrimary,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = ClayColors.GreyLight.copy(alpha = 0.6f),
            focusedLabelColor = accentColor,
            unfocusedLabelColor = ClayColors.TextSecondary,
            cursorColor = accentColor
        ),
        trailingIcon = trailingIcon
    )
}

/**
 * Indicador de Carregamento em Massinha de Modelar (ClayLoadingSpinner).
 * Utiliza 5 contas volumétricas de massinha dispostas em anel com física tátil de Stop-Motion,
 * efeito de elasticidade (squash & stretch), sombras suaves e rotação ondulada contínua.
 */
@Composable
fun ClayLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    dotSize: Dp = 12.dp,
    colors: List<Color> = listOf(
        ClayColors.Teal,
        ClayColors.Green,
        ClayColors.Orange,
        ClayColors.Blue,
        ClayColors.Gold
    ),
    message: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ClaySpinnerAnimation")

    // Rotação suave contínua do anel
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinnerRotation"
    )

    // Pulsação e onda elástica das bolinhas
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinnerWave"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { rotationZ = rotation },
            contentAlignment = Alignment.Center
        ) {
            val radiusPx = size.value * 0.35f
            val dotCount = colors.size

            colors.forEachIndexed { index, color ->
                val angle = (index.toFloat() / dotCount) * 2 * PI
                val individualWave = sin(wavePhase + index * (2 * PI / dotCount)).toFloat()
                
                // Simulação física de squash & stretch típica de massinha de modelar
                val scale = 0.85f + 0.35f * individualWave
                val shadowAlpha = 0.3f + 0.3f * individualWave

                val xOffset = (cos(angle) * radiusPx).dp
                val yOffset = (sin(angle) * radiusPx).dp

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .size(dotSize)
                        .scale(scale)
                        .shadow(
                            elevation = (3 + (individualWave * 2)).dp,
                            shape = CircleShape,
                            ambientColor = color.copy(alpha = shadowAlpha),
                            spotColor = color
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.55f),
                                    color,
                                    color.copy(alpha = 0.85f)
                                ),
                                center = Offset(dotSize.value * 0.3f, dotSize.value * 0.3f)
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                )
            }
        }

        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ClayColors.TextPrimary
            )
        }
    }
}

/**
 * Indicador linear de carregamento com 3 contas de massinha saltitantes (ClayLoadingDots).
 * Ideal para exibição dentro de botões (ex: "Carregando...") ou em cartões compactos.
 */
@Composable
fun ClayLoadingDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 10.dp,
    spacing: Dp = 6.dp,
    colors: List<Color> = listOf(
        ClayColors.Teal,
        ClayColors.Orange,
        ClayColors.Green
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ClayDotsAnimation")

    val offsets = colors.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 900
                    0f at 0
                    -8f at 300 using FastOutSlowInEasing
                    0f at 600 using FastOutSlowInEasing
                    0f at 900
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = androidx.compose.animation.core.StartOffset(index * 150)
            ),
            label = "DotOffset_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEachIndexed { index, color ->
            val yOffset = offsets[index].value
            val isRising = yOffset < -2f

            Box(
                modifier = Modifier
                    .offset(y = yOffset.dp)
                    .size(dotSize)
                    .graphicsLayer {
                        // Efeito de deformação de massinha ao subir/descer
                        scaleY = if (isRising) 1.2f else 0.9f
                        scaleX = if (isRising) 0.85f else 1.15f
                    }
                    .shadow(
                        elevation = if (isRising) 4.dp else 1.dp,
                        shape = CircleShape,
                        ambientColor = color.copy(alpha = 0.5f),
                        spotColor = color
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                color,
                                color.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(0.8.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
        }
    }
}

/**
 * Card completo com Spinner de massinha e mensagem explicativa de carregamento.
 */
@Composable
fun ClayLoadingCard(
    title: String = "Carregando...",
    description: String? = "Estamos preparando tudo para você!",
    modifier: Modifier = Modifier
) {
    ClayCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = Color.White,
        borderColor = ClayColors.TealLight,
        elevation = 6.dp,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClayLoadingSpinner(
                size = 56.dp,
                dotSize = 13.dp
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ClayColors.TextPrimary
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = ClayColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
