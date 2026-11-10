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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Smart & Advanced Modes",
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
            // Smart Music Playback Mode
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SMART MUSIC PLAYBACK MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SmartModeOption(
                        title = "Same Song Every Unlock",
                        subtitle = "Plays your primary chosen soundtrack reliably on every single unlock (Default)",
                        icon = Icons.Default.MusicNote,
                        selected = settings.playMode == "SAME_SONG",
                        onSelect = { viewModel.setPlayMode("SAME_SONG") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SmartModeOption(
                        title = "Random Song Mode",
                        subtitle = "Surprise yourself with a random track from your presets & imported library",
                        icon = Icons.Default.Shuffle,
                        selected = settings.playMode == "RANDOM_SONG",
                        onSelect = { viewModel.setPlayMode("RANDOM_SONG") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SmartModeOption(
                        title = "Time-Based Day/Night Soundtracks",
                        subtitle = "Morning (05-12h), Afternoon (12-18h), and Night (18-05h) customized tracks",
                        icon = Icons.Default.Brightness4,
                        selected = settings.playMode == "TIME_BASED",
                        onSelect = { viewModel.setPlayMode("TIME_BASED") }
                    )
                }
            }

            // Silent Mode Behavior
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SILENT & VIBRATE MODE BEHAVIOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RadioChoice(
                        title = "Respect Silent / Vibrate (Recommended)",
                        subtitle = "Skip unlock soundtrack when phone is muted or in vibrate mode",
                        selected = settings.silentModeBehavior == "RESPECT_SILENT",
                        onSelect = { viewModel.setSilentModeBehavior("RESPECT_SILENT") }
                    )

                    RadioChoice(
                        title = "Always Mute",
                        subtitle = "Never play audio when phone is in silent mode",
                        selected = settings.silentModeBehavior == "MUTE",
                        onSelect = { viewModel.setSilentModeBehavior("MUTE") }
                    )

                    RadioChoice(
                        title = "Play Anyway",
                        subtitle = "Override silent mode and play soundtrack at configured volume",
                        selected = settings.silentModeBehavior == "PLAY_ANYWAY",
                        onSelect = { viewModel.setSilentModeBehavior("PLAY_ANYWAY") }
                    )
                }
            }

            // Do Not Disturb Behavior
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DO NOT DISTURB (DND) BEHAVIOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleBright,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RadioChoice(
                        title = "Respect Do Not Disturb (Recommended)",
                        subtitle = "Skip soundtrack when DND or Priority mode is active",
                        selected = settings.dndBehavior == "RESPECT_DND",
                        onSelect = { viewModel.setDndBehavior("RESPECT_DND") }
                    )

                    RadioChoice(
                        title = "Play Over DND",
                        subtitle = "Continue playing unlock audio regardless of DND state",
                        selected = settings.dndBehavior == "PLAY_ANYWAY",
                        onSelect = { viewModel.setDndBehavior("PLAY_ANYWAY") }
                    )
                }
            }

            // Headphone & Bluetooth Preferences
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLUETOOTH & HEADPHONES PREFERENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RadioChoice(
                        title = "Play Anytime (Speaker & Headphones)",
                        subtitle = "Plays through whatever audio output is currently connected",
                        selected = settings.headphonePreference == "PLAY_ANYTIME",
                        onSelect = { viewModel.setHeadphonePreference("PLAY_ANYTIME") }
                    )

                    RadioChoice(
                        title = "Only When Headphones Connected",
                        subtitle = "Keeps unlock quiet unless wired or Bluetooth headphones are connected",
                        selected = settings.headphonePreference == "ONLY_HEADPHONES",
                        onSelect = { viewModel.setHeadphonePreference("ONLY_HEADPHONES") }
                    )

                    RadioChoice(
                        title = "Skip If Headphones Disconnected",
                        subtitle = "Prevents unexpected loud speaker output in public settings",
                        selected = settings.headphonePreference == "SKIP_IF_DISCONNECTED",
                        onSelect = { viewModel.setHeadphonePreference("SKIP_IF_DISCONNECTED") }
                    )
                }
            }

            // Audio Focus Behavior
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIO FOCUS BEHAVIOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurpleBright,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    RadioChoice(
                        title = "Stop When Another Music App Starts",
                        subtitle = "Yields immediately if Spotify, YouTube, or Podcast starts playing",
                        selected = settings.audioFocusBehavior == "PAUSE_ON_OTHER_AUDIO",
                        onSelect = { viewModel.setAudioFocusBehavior("PAUSE_ON_OTHER_AUDIO") }
                    )

                    RadioChoice(
                        title = "Duck Volume",
                        subtitle = "Lowers soundtrack volume to 30% while other media is active",
                        selected = settings.audioFocusBehavior == "DUCK_VOLUME",
                        onSelect = { viewModel.setAudioFocusBehavior("DUCK_VOLUME") }
                    )

                    RadioChoice(
                        title = "Play Over Other Media",
                        subtitle = "Plays soundtrack concurrently with background audio",
                        selected = settings.audioFocusBehavior == "PLAY_OVER",
                        onSelect = { viewModel.setAudioFocusBehavior("PLAY_OVER") }
                    )
                }
            }

            // Reset to defaults
            NeonButton(
                text = "Reset All Settings to Defaults",
                onClick = { viewModel.resetSettings() },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh,
                style = NeonButtonStyle.SECONDARY
            )
        }
    }
}

@Composable
private fun SmartModeOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected)
                    Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.15f), NeonPurple.copy(alpha = 0.15f)))
                else
                    Brush.linearGradient(listOf(Color(0xFF160F2B), Color(0xFF160F2B)))
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (selected) ElectricBlue else Color(0xFF281E48)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) AmoledBlack else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RadioChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = ElectricBlue,
                unselectedColor = TextSecondary
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
