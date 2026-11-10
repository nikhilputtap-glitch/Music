package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    val durationPresets = listOf(10, 15, 20, 30, 45, 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Playback Settings",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Duration Presets & Custom Slider
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PLAYBACK DURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current: ${settings.playbackDurationSeconds} seconds",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Audio automatically stops and releases memory after this duration.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durationPresets.forEach { sec ->
                            val isSelected = settings.playbackDurationSeconds == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.horizontalGradient(listOf(ElectricBlue, NeonPurple))
                                        else
                                            Brush.linearGradient(listOf(Color(0xFF22163E), Color(0xFF140E28)))
                                    )
                                    .clickable { viewModel.setDuration(sec) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AmoledBlack else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Custom Duration (5s to 180s)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = settings.playbackDurationSeconds.toFloat(),
                        onValueChange = { viewModel.setDuration(it.toInt()) },
                        valueRange = 5f..180f,
                        steps = 34,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = Color(0xFF241940)
                        )
                    )
                }
            }

            // Volume Setting
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PLAYBACK VOLUME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurpleBright,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Volume: ${(settings.volume * 100).toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = settings.volume,
                        onValueChange = { viewModel.setVolume(it) },
                        valueRange = 0.05f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonPurpleBright,
                            activeTrackColor = NeonPurpleBright,
                            inactiveTrackColor = Color(0xFF241940)
                        )
                    )
                }
            }

            // Fade-In Effect
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smooth Fade-In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Gently ramps volume from 0 to ${settings.fadeInDurationSeconds}s",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = settings.fadeInEnabled,
                            onCheckedChange = { viewModel.setFadeIn(it, settings.fadeInDurationSeconds) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmoledBlack,
                                checkedTrackColor = ElectricBlue,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = AmoledCard
                            )
                        )
                    }

                    if (settings.fadeInEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fade-In Duration: ${settings.fadeInDurationSeconds} seconds",
                            fontSize = 12.sp,
                            color = ElectricBlue
                        )
                        Slider(
                            value = settings.fadeInDurationSeconds.toFloat(),
                            onValueChange = { viewModel.setFadeIn(true, it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricBlue,
                                activeTrackColor = ElectricBlue,
                                inactiveTrackColor = Color(0xFF241940)
                            )
                        )
                    }
                }
            }

            // Fade-Out Effect
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smooth Fade-Out",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Prevents sudden audio cutoff before timer expires",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = settings.fadeOutEnabled,
                            onCheckedChange = { viewModel.setFadeOut(it, settings.fadeOutDurationSeconds) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmoledBlack,
                                checkedTrackColor = NeonPurpleBright,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = AmoledCard
                            )
                        )
                    }

                    if (settings.fadeOutEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fade-Out Duration: ${settings.fadeOutDurationSeconds} seconds",
                            fontSize = 12.sp,
                            color = NeonPurpleBright
                        )
                        Slider(
                            value = settings.fadeOutDurationSeconds.toFloat(),
                            onValueChange = { viewModel.setFadeOut(true, it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonPurpleBright,
                                activeTrackColor = NeonPurpleBright,
                                inactiveTrackColor = Color(0xFF241940)
                            )
                        )
                    }
                }
            }

            // Immediate Audio Test
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeonButton(
                    text = if (playbackState.isPlaying) "Playing (${playbackState.elapsedSeconds}s)" else "Test Current Settings",
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
        }
    }
}
