package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.SiriBlue
import com.example.ui.theme.SiriCyan
import com.example.ui.theme.SiriEmerald
import com.example.ui.theme.SiriPink
import com.example.ui.theme.SiriPurple

/**
 * VoiceOrb: The iconic Siri / Apple Intelligence inspired centerpiece.
 *
 * Visual States:
 * - IDLE: Slow, subtle breathing pulse with soft luminescent depth.
 * - ACTIVATED: High-energy vibrant emerald/cyan aura triggered by activation phrases.
 * - LISTENING: Rapid responsive pulse + dynamic concentric audio wave rings driven by mic RMS.
 * - THINKING: Fluid iridescent rotating aurora ring.
 * - SPEAKING: Vibrant magenta/cyan/purple breathing halo with harmonic fluid expansion.
 */
@Composable
fun VoiceOrb(
    state: AssistantState,
    rmsVolume: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Slow breathing pulse for IDLE
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Faster energetic pulse for LISTENING / SPEAKING / ACTIVATED
    val activePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_pulse"
    )

    // Continuous rotation for THINKING aurora
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_rotation"
    )

    // Wave ripples
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Smooth animated volume to prevent jitter
    val smoothedRms = remember { Animatable(0f) }
    LaunchedEffect(rmsVolume) {
        smoothedRms.animateTo(
            targetValue = rmsVolume,
            animationSpec = tween(durationMillis = 80, easing = LinearEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("voice_orb_container")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .testTag("voice_orb_canvas")
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.4f

            // Dynamic scale factor according to state and mic audio
            val currentScale = when (state) {
                AssistantState.IDLE -> idlePulse
                AssistantState.ACTIVATED -> activePulse * 1.12f
                AssistantState.LISTENING -> activePulse + (smoothedRms.value * 0.35f)
                AssistantState.THINKING -> idlePulse * 0.98f
                AssistantState.SPEAKING -> activePulse * 1.08f
                AssistantState.ERROR -> 0.95f
            }

            val dynamicRadius = baseRadius * currentScale

            // 1. Ambient Background Soft Glow
            val ambientColor = when (state) {
                AssistantState.IDLE -> SiriBlue.copy(alpha = 0.16f)
                AssistantState.ACTIVATED -> SiriEmerald.copy(alpha = 0.35f)
                AssistantState.LISTENING -> SiriCyan.copy(alpha = 0.28f + (smoothedRms.value * 0.2f))
                AssistantState.THINKING -> SiriPurple.copy(alpha = 0.32f)
                AssistantState.SPEAKING -> SiriPink.copy(alpha = 0.35f)
                AssistantState.ERROR -> Color(0xFFEF4444).copy(alpha = 0.25f)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambientColor, Color.Transparent),
                    center = center,
                    radius = baseRadius * 2.2f
                ),
                radius = baseRadius * 2.2f,
                center = center
            )

            // 2. Concentric Sound Wave Rings
            if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING || state == AssistantState.ACTIVATED) {
                val waveCount = 3
                for (i in 0 until waveCount) {
                    val progress = (wavePhase + (i.toFloat() / waveCount)) % 1f
                    val waveRadius = dynamicRadius + (progress * baseRadius * (0.8f + smoothedRms.value * 0.6f))
                    val waveAlpha = (1f - progress) * (0.6f + smoothedRms.value * 0.4f)

                    val waveColor = when (state) {
                        AssistantState.ACTIVATED -> SiriEmerald.copy(alpha = waveAlpha.coerceIn(0f, 0.85f))
                        AssistantState.LISTENING -> SiriCyan.copy(alpha = waveAlpha.coerceIn(0f, 0.85f))
                        else -> SiriPink.copy(alpha = waveAlpha.coerceIn(0f, 0.85f))
                    }

                    drawCircle(
                        color = waveColor,
                        radius = waveRadius,
                        center = center,
                        style = Stroke(width = (2.2f - progress * 1.2f).coerceAtLeast(0.8f).dp.toPx())
                    )
                }
            }

            // 3. Apple Intelligence Multi-Color Fluid Halo / Aurora
            rotate(rotation, pivot = center) {
                val haloColors = when (state) {
                    AssistantState.IDLE -> listOf(
                        SiriBlue.copy(alpha = 0.6f),
                        SiriPurple.copy(alpha = 0.5f),
                        SiriCyan.copy(alpha = 0.6f),
                        SiriBlue.copy(alpha = 0.6f)
                    )
                    AssistantState.ACTIVATED -> listOf(
                        SiriEmerald,
                        SiriCyan,
                        SiriPurple,
                        SiriEmerald
                    )
                    AssistantState.LISTENING -> listOf(
                        SiriCyan,
                        SiriEmerald,
                        SiriBlue,
                        SiriCyan
                    )
                    AssistantState.THINKING -> listOf(
                        SiriPurple,
                        SiriPink,
                        SiriCyan,
                        SiriBlue,
                        SiriPurple
                    )
                    AssistantState.SPEAKING -> listOf(
                        SiriPink,
                        SiriPurple,
                        SiriCyan,
                        SiriPink
                    )
                    AssistantState.ERROR -> listOf(
                        Color(0xFFEF4444),
                        Color(0xFFF97316),
                        Color(0xFFEF4444)
                    )
                }

                drawCircle(
                    brush = Brush.sweepGradient(haloColors, center = center),
                    radius = dynamicRadius * 1.08f,
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            // 4. Center Glowing Sphere Body with Multi-stop Radial Shader
            val sphereColors = when (state) {
                AssistantState.IDLE -> listOf(
                    Color.White.copy(alpha = 0.95f),
                    SiriCyan.copy(alpha = 0.85f),
                    SiriBlue.copy(alpha = 0.75f),
                    SiriPurple.copy(alpha = 0.4f),
                    Color(0xFF0D1117)
                )
                AssistantState.ACTIVATED -> listOf(
                    Color.White,
                    SiriEmerald,
                    SiriCyan,
                    Color(0xFF064E3B)
                )
                AssistantState.LISTENING -> listOf(
                    Color.White,
                    SiriCyan,
                    SiriBlue,
                    Color(0xFF0369A1)
                )
                AssistantState.THINKING -> listOf(
                    Color.White.copy(alpha = 0.9f),
                    SiriPurple,
                    SiriPink,
                    Color(0xFF4C1D95)
                )
                AssistantState.SPEAKING -> listOf(
                    Color.White,
                    SiriPink,
                    SiriPurple,
                    Color(0xFF831843)
                )
                AssistantState.ERROR -> listOf(
                    Color.White,
                    Color(0xFFF87171),
                    Color(0xFFB91C1C)
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = sphereColors,
                    center = Offset(center.x - dynamicRadius * 0.2f, center.y - dynamicRadius * 0.25f),
                    radius = dynamicRadius * 1.1f
                ),
                radius = dynamicRadius,
                center = center
            )

            // 5. Specular Apple Glass Arc Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
                    center = Offset(center.x - dynamicRadius * 0.35f, center.y - dynamicRadius * 0.35f),
                    radius = dynamicRadius * 0.5f
                ),
                radius = dynamicRadius * 0.45f,
                center = Offset(center.x - dynamicRadius * 0.25f, center.y - dynamicRadius * 0.25f),
                blendMode = BlendMode.Plus
            )
        }
    }
}
