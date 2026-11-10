package com.example.data.model

data class UnlockSettings(
    val autoPlayEnabled: Boolean = true,
    val playbackDurationSeconds: Int = 30, // 10, 15, 20, 30, 45, 60, custom
    val volume: Float = 1.0f,
    val fadeInEnabled: Boolean = true,
    val fadeInDurationSeconds: Int = 2,
    val fadeOutEnabled: Boolean = true,
    val fadeOutDurationSeconds: Int = 3,
    val silentModeBehavior: String = "RESPECT_SILENT", // RESPECT_SILENT, MUTE, PLAY_ANYWAY
    val dndBehavior: String = "RESPECT_DND", // RESPECT_DND, PLAY_ANYWAY
    val headphonePreference: String = "PLAY_ANYTIME", // PLAY_ANYTIME, ONLY_HEADPHONES, SKIP_IF_DISCONNECTED
    val audioFocusBehavior: String = "PAUSE_ON_OTHER_AUDIO", // PAUSE_ON_OTHER_AUDIO, DUCK_VOLUME, PLAY_OVER
    val playMode: String = "SAME_SONG", // SAME_SONG, RANDOM_SONG, TIME_BASED
    val selectedSongId: String? = null,
    val morningSongId: String? = null,
    val afternoonSongId: String? = null,
    val nightSongId: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val totalUnlocksTriggered: Int = 0,
    val lastUnlockTimestamp: Long = 0L
)
