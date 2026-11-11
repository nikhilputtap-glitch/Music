package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.components.StatusBadge
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaybackSettings: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val selectedSong by viewModel.selectedSong.collectAsState()
    val monitoringStatus by viewModel.monitoringStatus.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val durationPresets = listOf(10, 15, 20, 30, 45, 60)
    val scope = rememberCoroutineScope()
    var isSimulatingUnlock by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "UNLOCK TUNES",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Every Unlock Deserves a Soundtrack.",
                            fontSize = 11.sp,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onNavigateToPermissions,
                        modifier = Modifier.testTag("permissions_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Permissions Guide",
                            tint = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.testTag("about_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About App",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Monitoring Status Banner & Master Toggle
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                highlightGlow = settings.autoPlayEnabled
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            StatusBadge(status = monitoringStatus)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (settings.autoPlayEnabled) "Auto-Soundtrack Active" else "Auto-Soundtrack Paused",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (settings.autoPlayEnabled)
                                    "Plays soundtrack for ${settings.playbackDurationSeconds}s on phone unlock"
                                else
                                    "Tap toggle to enable soundtrack on unlock",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = settings.autoPlayEnabled,
                            onCheckedChange = { viewModel.toggleAutoPlay() },
                            modifier = Modifier.testTag("auto_play_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmoledBlack,
                                checkedTrackColor = ElectricBlue,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = AmoledCard
                            )
                        )
                    }
                }
            }
        }

        // Selected Song Info Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPlayer() }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Spinning Vinyl or Album Artwork
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(NeonPurple, AmoledBlack)))
                                .border(2.dp, ElectricBlue, CircleShape)
                                .rotate(if (playbackState.isPlaying) spinAngle else 0f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = "Vinyl",
                                tint = ElectricBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(AmoledBlack)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SELECTED SOUNDTRACK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPurpleBright,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (selectedSong?.isPreset == true) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonPurple.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PRESET", fontSize = 9.sp, color = NeonPurpleBright, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = selectedSong?.title ?: "No Song Selected",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${selectedSong?.artist ?: "Unknown"} • ${selectedSong?.formattedDuration ?: "--:--"}",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons inside song card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeonButton(
                            text = "Change Song",
                            onClick = onNavigateToLibrary,
                            style = NeonButtonStyle.SECONDARY,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LibraryMusic,
                            testTag = "change_song_button"
                        )

                        NeonButton(
                            text = "Full Player",
                            onClick = onNavigateToPlayer,
                            style = NeonButtonStyle.SECONDARY,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.GraphicEq,
                            testTag = "full_player_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Explicit "SET / ACTIVE AS UNLOCK SOUNDTRACK" Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (settings.autoPlayEnabled)
                                    Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.2f), ElectricBlue.copy(alpha = 0.2f)))
                                else
                                    Brush.horizontalGradient(listOf(ElectricBlue, NeonPurple))
                            )
                            .clickable {
                                selectedSong?.let { viewModel.activateSongForUnlock(it.id) }
                            }
                            .padding(vertical = 11.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (settings.autoPlayEnabled) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (settings.autoPlayEnabled) NeonGreen else AmoledBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (settings.autoPlayEnabled)
                                    "✓ ACTIVE: WILL PLAY AUTOMATICALLY ON PHONE UNLOCK"
                                else
                                    "⚡ SET THIS SONG AS UNLOCK SOUNDTRACK",
                                color = if (settings.autoPlayEnabled) NeonGreen else AmoledBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Real Phone Testing Step-by-Step Guide
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF140D2B)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WORKS AUTOMATICALLY EVEN WHEN APP IS CLOSED:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "1. Press your phone's Power button to Lock\n2. Unlock your phone (Fingerprint/PIN)\n3. Song will play automatically for 30s every time!",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Live Audio Waveform & Elapsed Countdown
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (playbackState.isPlaying) "NOW PLAYING SOUNDTRACK" else "WAVEFORM PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playbackState.isPlaying) ElectricBlue else TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (playbackState.isPlaying)
                                "${playbackState.elapsedSeconds}s / ${playbackState.totalDurationSeconds}s"
                            else
                                "Configured: ${settings.playbackDurationSeconds}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playbackState.isPlaying) NeonGreen else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    WaveformVisualizer(
                        isPlaying = playbackState.isPlaying,
                        barCount = 32,
                        height = 54.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Controls: Play Test & Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonButton(
                            text = if (playbackState.isPlaying) "Soundtrack Playing..." else "Play Test",
                            onClick = { viewModel.playTestSoundtrack() },
                            modifier = Modifier.weight(1.5f),
                            icon = Icons.Default.PlayArrow,
                            style = NeonButtonStyle.PRIMARY,
                            testTag = "play_test_button"
                        )

                        NeonButton(
                            text = "Stop",
                            onClick = { viewModel.stopPlayback() },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Stop,
                            style = NeonButtonStyle.DANGER,
                            enabled = playbackState.isPlaying,
                            testTag = "stop_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // In-App Simulated Lock/Unlock Test Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF281848), Color(0xFF160E2A))))
                            .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    isSimulatingUnlock = true
                                    delay(1200)
                                    isSimulatingUnlock = false
                                    viewModel.playTestSoundtrack()
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSimulatingUnlock) "Simulating Phone Lock/Unlock..." else "Simulate Lock & Unlock Sequence",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }
        }

        // Quick Duration Selector Chips
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLAYBACK DURATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleBright,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Custom Settings >",
                            fontSize = 11.sp,
                            color = ElectricBlue,
                            modifier = Modifier.clickable { onNavigateToPlaybackSettings() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(durationPresets) { seconds ->
                            val isSelected = settings.playbackDurationSeconds == seconds
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.horizontalGradient(listOf(ElectricBlue, NeonPurple))
                                        else
                                            Brush.linearGradient(listOf(Color(0xFF20163B), Color(0xFF140D26)))
                                    )
                                    .clickable { viewModel.setDuration(seconds) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (seconds == 30) "${seconds}s (Default)" else "${seconds}s",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AmoledBlack else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats & Smart Mode status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TOTAL UNLOCKS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${settings.totalUnlocksTriggered}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                        Text("Soundtracks played", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("SMART MODE", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (settings.playMode) {
                                "RANDOM_SONG" -> "Random"
                                "TIME_BASED" -> "Time-Based"
                                else -> "Same Song"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleBright
                        )
                        Text(
                            text = if (settings.lastUnlockTimestamp > 0)
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(settings.lastUnlockTimestamp))
                            else "No unlocks yet",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Navigation Grid Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SETTINGS & FEATURES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureShortcutCard(
                        title = "Playback Settings",
                        subtitle = "Fade In/Out & Volume",
                        icon = Icons.Default.Tune,
                        iconColor = ElectricBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPlaybackSettings
                    )

                    FeatureShortcutCard(
                        title = "Smart & Advanced",
                        subtitle = "Time-Based, DND & Silence",
                        icon = Icons.Default.Settings,
                        iconColor = NeonPurpleBright,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAdvancedSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
        }
    }
}
