package com.ravi.chatora.presentation.chatora.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ravi.chatora.domain.models.VoiceState
import com.ravi.chatora.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun VoiceChatDialog(
    voiceState: VoiceState,
    onStartListening: () -> Unit = {},
    onStopListening: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) { onStartListening() }

    val scope = rememberCoroutineScope()
    val btnScale = remember { Animatable(1f) }

    // Pulse rings — only animate while listening
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart
        ), label = "p1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            tween(1200, delayMillis = 300, easing = FastOutSlowInEasing), RepeatMode.Restart
        ), label = "p2"
    )
    val pulse1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing), RepeatMode.Restart
        ), label = "a1"
    )
    val pulse2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.18f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1200, delayMillis = 300, easing = LinearEasing), RepeatMode.Restart
        ), label = "a2"
    )

    // Waveform bar heights for "listening" visual
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 80), RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(350, delayMillis = 160), RepeatMode.Reverse), label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, delayMillis = 40), RepeatMode.Reverse), label = "b4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(380, delayMillis = 200), RepeatMode.Reverse), label = "b5"
    )

    val isListening = voiceState.isSpeaking
    val hasText = voiceState.spokenText.isNotEmpty()

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = AppColors.Surface,
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Header label ───────────────────────────────────────
                    AnimatedContent(
                        targetState = when {
                            hasText -> "Ready to send"
                            isListening -> "Listening…"
                            else -> "Tap to speak"
                        },
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                        label = "statusLabel"
                    ) { label ->
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isListening && !hasText) AppColors.Primary
                            else AppColors.OnSurfaceVariant,
                            letterSpacing = 0.3.sp
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Mic orb with pulse rings ───────────────────────────
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer pulse ring 2
                        if (isListening && !hasText) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .scale(pulse2)
                                    .graphicsLayer { alpha = pulse2Alpha }
                                    .background(
                                        AppColors.Primary.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                            )
                            // Inner pulse ring 1
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .scale(pulse1)
                                    .graphicsLayer { alpha = pulse1Alpha }
                                    .background(
                                        AppColors.Primary.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            )
                        }

                        // Core orb
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(
                                    brush = when {
                                        hasText -> Brush.linearGradient(
                                            listOf(AppColors.Success, Color(0xFF00A843))
                                        )
                                        isListening -> Brush.linearGradient(
                                            listOf(AppColors.Primary, AppColors.Tertiary)
                                        )
                                        else -> Brush.linearGradient(
                                            listOf(
                                                AppColors.SurfaceContainerHigh,
                                                AppColors.SurfaceContainer
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (!isListening && !hasText) 1.dp else 0.dp,
                                    color = AppColors.OutlineVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = hasText,
                                transitionSpec = { fadeIn(spring()) togetherWith fadeOut(tween(150)) },
                                label = "orbIcon"
                            ) { textReady ->
                                if (textReady) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = if (isListening) Color.White
                                        else AppColors.OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Waveform / spoken text ─────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = hasText,
                            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                            label = "waveOrText"
                        ) { showText ->
                            if (showText) {
                                Text(
                                    text = voiceState.spokenText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    color = AppColors.OnSurface,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3
                                )
                            } else {
                                // Animated waveform bars
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(bar1, bar2, bar3, bar4, bar5).forEach { fraction ->
                                        val barColor = if (isListening) AppColors.Primary else AppColors.OutlineVariant
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height((fraction * 32).dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(barColor)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Action buttons ─────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel / Dismiss
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    btnScale.animateTo(0.92f, tween(80))
                                    btnScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                                    onCancelClick()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .scale(btnScale.value),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.OnSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, AppColors.OutlineVariant
                            )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Primary action — changes by state
                        val primaryLabel = when {
                            hasText -> "Use this"
                            isListening -> "Stop"
                            else -> "Listen"
                        }
                        val primaryBg = when {
                            hasText -> AppColors.Primary
                            isListening -> AppColors.Error
                            else -> AppColors.Primary
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    btnScale.animateTo(0.92f, tween(80))
                                    btnScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                                    when {
                                        hasText -> onSubmitClick()
                                        isListening -> onStopListening()
                                        else -> onStartListening()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .scale(btnScale.value),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryBg,
                                contentColor = Color.White
                            )
                        ) {
                            AnimatedContent(
                                targetState = primaryLabel,
                                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
                                label = "btnLabel"
                            ) { label ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            hasText -> Icons.Default.Check
                                            isListening -> Icons.Default.MicOff
                                            else -> Icons.Default.Mic
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Error hint
                    if (voiceState.error != null) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = voiceState.error,
                            fontSize = 12.sp,
                            color = AppColors.Error.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}