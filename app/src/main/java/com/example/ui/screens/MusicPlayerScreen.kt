package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val selectedSong by viewModel.selectedSong.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "player_vinyl")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Active Player",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Turntable / Vinyl
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(if (playbackState.isPlaying) pulseScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer neon aura
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    ElectricBlue.copy(alpha = if (playbackState.isPlaying) 0.35f else 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Vinyl Body
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF140D26),
                                    Color(0xFF251842),
                                    Color(0xFF140D26),
                                    Color(0xFF332059),
                                    Color(0xFF140D26)
                                )
                            )
                        )
                        .border(3.dp, ElectricBlue.copy(alpha = 0.7f), CircleShape)
                        .rotate(if (playbackState.isPlaying) spinAngle else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    // Grooves
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .border(1.dp, Color(0x3300F0FF), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .border(1.dp, Color(0x33C77DFF), CircleShape)
                    )

                    // Center label
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricBlue, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = AmoledBlack,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Track Meta
            Text(
                text = selectedSong?.title ?: "No Song Selected",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${selectedSong?.artist ?: "Unknown Artist"} • ${selectedSong?.genre ?: "Audio"}",
                fontSize = 14.sp,
                color = ElectricBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform
            WaveformVisualizer(
                isPlaying = playbackState.isPlaying,
                modifier = Modifier.fillMaxWidth(),
                barCount = 36,
                height = 64.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Countdown Progress
            val progress = if (playbackState.totalDurationSeconds > 0) {
                playbackState.elapsedSeconds.toFloat() / playbackState.totalDurationSeconds
            } else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricBlue,
                trackColor = Color(0xFF1E1538)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${playbackState.elapsedSeconds}s",
                    fontSize = 12.sp,
                    color = if (playbackState.isPlaying) NeonGreen else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configured: ${settings.playbackDurationSeconds}s",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Play/Stop Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonButton(
                    text = if (playbackState.isPlaying) "Soundtrack Active" else "Play Unlock Test",
                    onClick = { viewModel.playTestSoundtrack() },
                    modifier = Modifier.weight(1.5f),
                    icon = Icons.Default.PlayArrow,
                    style = NeonButtonStyle.PRIMARY
                )

                NeonButton(
                    text = "Stop",
                    onClick = { viewModel.stopPlayback() },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Stop,
                    style = NeonButtonStyle.DANGER,
                    enabled = playbackState.isPlaying
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Volume Control
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Playback Volume",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${(settings.volume * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }

                    Slider(
                        value = settings.volume,
                        onValueChange = { viewModel.setVolume(it) },
                        valueRange = 0.05f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = Color(0xFF281E46)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata & Engine Diagnostics
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AUDIO ENGINE SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Decoder: Android MediaPlayer with AudioAttributes.USAGE_MEDIA\n" +
                               "• Audio Focus: Transient with ducking support\n" +
                               "• Fade Engine: Active (${settings.fadeInDurationSeconds}s in / ${settings.fadeOutDurationSeconds}s out)\n" +
                               "• Storage Protocol: SAF Persistable URI Grant\n" +
                               "• Unlock Trigger: BroadcastReceiver (ACTION_USER_PRESENT)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
