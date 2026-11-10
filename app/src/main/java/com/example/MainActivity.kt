package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.UnlockTunesTheme
import com.example.ui.viewmodel.MainViewModel

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

        setContent {
            UnlockTunesTheme {
                MainAppNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSongs()
    }
}

@Composable
fun MainAppNavHost(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

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
        }
    }
}
