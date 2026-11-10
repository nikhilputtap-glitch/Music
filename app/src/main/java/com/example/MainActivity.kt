package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.UnlockPlaybackService
import com.example.ui.screens.AboutPrivacyScreen
import com.example.ui.screens.AdvancedSettingsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PermissionsSetupScreen
import com.example.ui.screens.PlaybackSettingsScreen
import com.example.ui.screens.SongSelectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UnlockTunesTheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

enum class Screen {
    SPLASH,
    ONBOARDING,
    HOME,
    SONG_SELECTION,
    PLAYER,
    PLAYBACK_SETTINGS,
    ADVANCED_SETTINGS,
    PERMISSIONS,
    ABOUT
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Automatically start unlock monitoring service
        UnlockPlaybackService.startMonitoring(this)

        setContent {
            UnlockTunesTheme {
                MainAppNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSongs()
        UnlockPlaybackService.startMonitoring(this)
    }
}

@Composable
fun MainAppNavHost(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(4500)
            viewModel.clearNotificationMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = AmoledBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AmoledBlack)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.SPLASH -> {
                        SplashScreen(
                            onNavigateNext = {
                                if (settings.hasCompletedOnboarding) {
                                    currentScreen = Screen.HOME
                                } else {
                                    currentScreen = Screen.ONBOARDING
                                }
                            }
                        )
                    }

                    Screen.ONBOARDING -> {
                        OnboardingScreen(
                            onFinished = {
                                viewModel.completeOnboarding()
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToLibrary = { currentScreen = Screen.SONG_SELECTION },
                            onNavigateToPlayer = { currentScreen = Screen.PLAYER },
                            onNavigateToPlaybackSettings = { currentScreen = Screen.PLAYBACK_SETTINGS },
                            onNavigateToAdvancedSettings = { currentScreen = Screen.ADVANCED_SETTINGS },
                            onNavigateToPermissions = { currentScreen = Screen.PERMISSIONS },
                            onNavigateToAbout = { currentScreen = Screen.ABOUT }
                        )
                    }

                    Screen.SONG_SELECTION -> {
                        BackHandler { currentScreen = Screen.HOME }
                        SongSelectionScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.PLAYER -> {
                        BackHandler { currentScreen = Screen.HOME }
                        MusicPlayerScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.PLAYBACK_SETTINGS -> {
                        BackHandler { currentScreen = Screen.HOME }
                        PlaybackSettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.ADVANCED_SETTINGS -> {
                        BackHandler { currentScreen = Screen.HOME }
                        AdvancedSettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.PERMISSIONS -> {
                        BackHandler { currentScreen = Screen.HOME }
                        PermissionsSetupScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }

                    Screen.ABOUT -> {
                        BackHandler { currentScreen = Screen.HOME }
                        AboutPrivacyScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }
                }
            }

            // Global Confirmation Banner for "Song Set for Unlock"
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AmoledCard, Color(0xFF1E1438))
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(listOf(NeonGreen, ElectricBlue)),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.clearNotificationMessage() }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = notificationMessage ?: "",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearNotificationMessage() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
