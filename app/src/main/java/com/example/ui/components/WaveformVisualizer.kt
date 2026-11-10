package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    height: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 1200 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 400 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val totalSpacing = canvasWidth * 0.25f
        val availableBarWidth = canvasWidth - totalSpacing
        val barWidth = availableBarWidth / barCount
        val barSpacing = totalSpacing / (barCount - 1).coerceAtLeast(1)

        val brush = Brush.verticalGradient(
            colors = listOf(ElectricBlue, NeonPurple, NeonMagenta)
        )

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            val wave1 = sin(phase + normalizedIndex * 4f)
            val wave2 = sin(phase * 1.5f + normalizedIndex * 8f)
            
            val amplitude = if (isPlaying) {
                val raw = (abs(wave1 * 0.6f + wave2 * 0.4f) * bounce).coerceIn(0.15f, 1.0f)
                raw
            } else {
                (0.12f + 0.10f * sin(phase * 0.5f + normalizedIndex * 2f)).coerceIn(0.08f, 0.25f)
            }

            val barH = canvasHeight * amplitude
            val left = i * (barWidth + barSpacing)
            val top = (canvasHeight - barH) / 2f

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
