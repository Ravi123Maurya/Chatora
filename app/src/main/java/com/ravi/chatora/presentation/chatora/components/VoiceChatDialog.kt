package com.ravi.chatora.presentation.chatora.components

import android.app.Dialog
import android.graphics.drawable.shapes.OvalShape
import android.util.Log
import androidx.collection.intIntMapOf
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ravi.chatora.domain.models.VoiceState
import kotlinx.coroutines.launch


@Composable
fun VoiceChatDialog(
    voiceState: VoiceState,
    onStartListening: () -> Unit = {},
    onStopListening: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {

    val infiniteTransition = rememberInfiniteTransition()
    val animateScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(voiceState) {

        Log.d(
            "VoiceChatDebug",
            "VoiceState: ${voiceState.isSpeaking} -- ${voiceState.error} -- ${voiceState.spokenText} "
        )
    }

    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }


    LaunchedEffect(Unit) { onStartListening() }

    Dialog(
        onDismissRequest = {}
    ) {

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.padding(all = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(80.dp * animateScale)
                                .clip(CircleShape)
                                .background(Color.Blue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(38.dp),
                                imageVector = Icons.Default.Mic,
                                contentDescription = "mic",
                                tint = Color.White
                            )
                        }
                    }

                    if (voiceState.spokenText.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(top = 48.dp),
                            text = if (voiceState.isSpeaking) "Listening..." else "Talk With Chatora",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = voiceState.spokenText,
                                maxLines = 4,
                            )
                        }
                    }


                    Spacer(Modifier.height(48.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            modifier = Modifier
                                .scale(scale.value)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.1f)),
                            onClick = {
                                scope.launch {
                                    scale.animateTo(0.9f)
                                    scale.animateTo(1.2f)
                                    scale.animateTo(1f)
                                    onCancelClick()
                                }

                            }
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "cancel")
                        }

                        IconButton(
                            modifier = Modifier
                                .scale(scale.value)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.1f)),
                            onClick = {
                                scope.launch {
                                    scale.animateTo(0.9f)
                                    scale.animateTo(1.2f)
                                    scale.animateTo(1f)
                                    when{
                                        voiceState.spokenText.isNotEmpty() -> onSubmitClick()
                                        voiceState.isSpeaking -> onStopListening()
                                        else -> onStartListening()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = when{
                                    voiceState.spokenText.isNotEmpty() -> Icons.Default.Check
                                    voiceState.isSpeaking -> Icons.Default.Mic
                                    else -> Icons.Default.MicOff
                                },
                                contentDescription = "mic",
                                tint = when{
                                    voiceState.spokenText.isNotEmpty() -> Color.Green
                                    voiceState.isSpeaking -> Color.Black
                                    else -> Color.Red
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}