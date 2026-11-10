package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPrivacyScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "About & Privacy",
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
            // App Branding Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Unlock Tunes",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "UNLOCK TUNES",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Every Unlock Deserves a Soundtrack.",
                        fontSize = 13.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0 • Package: com.nikhil.unlocktunes",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Zero-Data Privacy Commitment
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zero-Data Privacy Commitment",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "100% Local On-Device Execution",
                                fontSize = 11.sp,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PrivacyBullet("Zero Internet Access", "The app does not have or request the INTERNET permission in AndroidManifest.xml. It is technically impossible for the app to transmit data.")
                    PrivacyBullet("No Server Music Uploads", "Your selected audio files remain securely on your phone's storage. Nothing is ever uploaded to any cloud server.")
                    PrivacyBullet("No Tracking or Analytics", "No ad IDs, no analytics SDKs, no behavioral trackers.")
                    PrivacyBullet("No Background Microphone Access", "The app only monitors system unlock events (ACTION_USER_PRESENT) and does not record audio.")
                }
            }

            // QA & Verification Checklist
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ENGINEERING TESTING CHECKLIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ChecklistRow("Normal Unlock Trigger", "Debounced BroadcastReceiver triggers upon ACTION_USER_PRESENT.")
                    ChecklistRow("30s Elapsed Countdown", "Accurate SystemClock.elapsedRealtime timing prevents drift.")
                    ChecklistRow("Fade-In & Fade-Out", "Volume ramps smoothly over 2s start and 3s before stop.")
                    ChecklistRow("Duplicate Protection", "Debounce threshold discards unlocks within 3 seconds.")
                    ChecklistRow("Silent / DND Gate", "Respects RINGER_MODE_SILENT and interruption filter.")
                    ChecklistRow("Headphone Modes", "Detects Bluetooth A2DP and wired headsets dynamically.")
                    ChecklistRow("Device Boot Recovery", "BOOT_COMPLETED receiver restores presets & settings.")
                    ChecklistRow("Memory & Player Release", "MediaPlayer released cleanly without resource leaks.")
                }
            }

            // Developer & Architecture Info
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ARCHITECTURE & CREDITS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Built natively using Kotlin 2.2, Jetpack Compose, Material Design 3, DataStore Preferences, and Android Media Framework.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyBullet(
    title: String,
    detail: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(text = detail, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(start = 22.dp))
    }
}

@Composable
private fun ChecklistRow(
    title: String,
    detail: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Text(text = detail, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(start = 20.dp))
    }
}
