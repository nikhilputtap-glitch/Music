package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSetupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else true
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Permissions & Reliability",
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
            // Header summary
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANDROID COMPATIBILITY ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlock Soundtracks on Android 13, 14, 15 & 16",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To guarantee prompt playback the moment your phone is unlocked, Android requires notification controls and unrestricted background execution. All features run 100% locally.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            // 1. Notification Permission Card
            PermissionCard(
                title = "Playback Controls & Notification",
                status = if (hasNotificationPermission) "Granted & Active" else "Permission Required",
                isGranted = hasNotificationPermission,
                description = "Required on Android 13+ to display the active soundtrack notification with the instant Stop button.",
                icon = Icons.Default.Notifications,
                buttonText = if (hasNotificationPermission) "Granted" else "Allow Notifications",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            // 2. Battery Optimization Exemption Card
            PermissionCard(
                title = "Unrestricted Battery Background",
                status = if (isIgnoringBatteryOptimizations) "Unrestricted (Optimized)" else "Standard Optimization",
                isGranted = isIgnoringBatteryOptimizations,
                description = "Prevents aggressive OEM battery management from delaying the unlock trigger after hours in sleep/Doze mode.",
                icon = Icons.Default.BatteryChargingFull,
                buttonText = if (isIgnoringBatteryOptimizations) "Configured" else "Exempt from Battery Saver",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                }
            )

            // 3. Modern Zero Storage Permission Architecture
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
                            Icon(Icons.Default.Folder, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zero Storage Permissions Needed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Google Play Compliant Storage Access",
                                fontSize = 11.sp,
                                color = NeonGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Unlock Tunes never requests broad storage permissions (READ_EXTERNAL_STORAGE). Instead, it uses Android's Storage Access Framework (SAF) with persistable URI grants, allowing you to select only the exact songs you want to play without giving app-wide storage access.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            // Manufacturer specific advice
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANUFACTURER-SPECIFIC TROUBLESHOOTING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ManufacturerItem(
                        brand = "Samsung (One UI)",
                        instruction = "Go to Settings > Apps > UNLOCK TUNES > Battery > Set to 'Unrestricted'. Also ensure app is not in 'Sleeping apps'."
                    )

                    ManufacturerItem(
                        brand = "Xiaomi (MIUI / HyperOS)",
                        instruction = "Settings > Apps > Manage Apps > UNLOCK TUNES > Enable 'Autostart' and set Battery Saver to 'No Restrictions'."
                    )

                    ManufacturerItem(
                        brand = "OnePlus / Oppo / Realme (OxygenOS)",
                        instruction = "Settings > Battery > App Battery Management > UNLOCK TUNES > Allow Foreground Activity & Auto Launch."
                    )

                    ManufacturerItem(
                        brand = "Google Pixel (Stock Android)",
                        instruction = "App Info > App Battery Usage > Set to 'Unrestricted'. All unlock broadcasts work out of the box."
                    )
                }
            }

            // App info settings button
            NeonButton(
                text = "Open System App Settings",
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.OpenInNew,
                style = NeonButtonStyle.SECONDARY
            )
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    status: String,
    isGranted: Boolean,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onAction: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        highlightGlow = !isGranted
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) NeonGreen.copy(alpha = 0.15f) else Color(0xFFFF9100).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (isGranted) NeonGreen else Color(0xFFFF9100),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGranted) NeonGreen else Color(0xFFFF9100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = description, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(14.dp))

            NeonButton(
                text = buttonText,
                onClick = onAction,
                enabled = !isGranted,
                style = if (isGranted) NeonButtonStyle.SECONDARY else NeonButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ManufacturerItem(
    brand: String,
    instruction: String
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = brand, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
        Text(text = instruction, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
    }
}
