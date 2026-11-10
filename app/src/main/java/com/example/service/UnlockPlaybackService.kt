package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.UnlockAudioPlayerManager
import com.example.data.model.SongItem
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class UnlockPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var playerManager: UnlockAudioPlayerManager
    private lateinit var settingsRepository: SettingsRepository
    private var wakeLock: PowerManager.WakeLock? = null
    private var notificationJob: Job? = null

    companion object {
        const val ACTION_START_UNLOCK_PLAYBACK = "com.nikhil.unlocktunes.START_PLAYBACK"
        const val ACTION_STOP_PLAYBACK = "com.nikhil.unlocktunes.STOP_PLAYBACK"
        const val ACTION_TEST_PLAYBACK = "com.nikhil.unlocktunes.TEST_PLAYBACK"

        const val CHANNEL_ID = "unlock_tunes_playback_channel"
        const val NOTIFICATION_ID = 1001

        fun startPlayback(context: Context, isTest: Boolean = false) {
            val intent = Intent(context, UnlockPlaybackService::class.java).apply {
                action = if (isTest) ACTION_TEST_PLAYBACK else ACTION_START_UNLOCK_PLAYBACK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopPlayback(context: Context) {
            val intent = Intent(context, UnlockPlaybackService::class.java).apply {
                action = ACTION_STOP_PLAYBACK
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        playerManager = UnlockAudioPlayerManager.getInstance(this)
        settingsRepository = SettingsRepository(this)
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "UnlockTunes:PlaybackWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_PLAYBACK -> {
                handleStop()
                return START_NOT_STICKY
            }
            ACTION_START_UNLOCK_PLAYBACK -> {
                handleStartPlayback(isTest = false)
            }
            ACTION_TEST_PLAYBACK -> {
                handleStartPlayback(isTest = true)
            }
            else -> {
                handleStartPlayback(isTest = false)
            }
        }
        return START_NOT_STICKY
    }

    private fun handleStartPlayback(isTest: Boolean) {
        serviceScope.launch {
            val settings = settingsRepository.getSettingsSnapshot()

            // If not a test, check if playback is allowed
            if (!isTest) {
                val (allowed, reason) = playerManager.shouldAllowPlayback(settings)
                if (!allowed) {
                    stopSelf()
                    return@launch
                }
            }

            // Determine which song to play
            val songToPlay = pickSong(settings)
            if (songToPlay == null) {
                stopSelf()
                return@launch
            }

            // Acquire wakelock safely with timeout
            try {
                wakeLock?.acquire((settings.playbackDurationSeconds + 5) * 1000L)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Start foreground service with notification
            val initialNotification = buildNotification(songToPlay, 0, settings.playbackDurationSeconds)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this@UnlockPlaybackService,
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }

            // Record unlock event if not test
            if (!isTest) {
                settingsRepository.recordUnlockTriggered()
            }

            // Start player
            playerManager.playSong(songToPlay, settings) {
                handlePlaybackFinished()
            }

            // Observe playback state to update notification countdown
            observePlaybackProgress(songToPlay)
        }
    }

    private fun observePlaybackProgress(song: SongItem) {
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            playerManager.playbackState.collectLatest { state ->
                if (!state.isPlaying) {
                    handlePlaybackFinished()
                } else {
                    val updatedNotification = buildNotification(
                        song,
                        state.elapsedSeconds,
                        state.totalDurationSeconds
                    )
                    notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private suspend fun pickSong(settings: com.example.data.model.UnlockSettings): SongItem? {
        val allSongs = settingsRepository.getAllSongs()
        if (allSongs.isEmpty()) return null

        return when (settings.playMode) {
            "RANDOM_SONG" -> {
                allSongs.randomOrNull() ?: allSongs.firstOrNull()
            }
            "TIME_BASED" -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val targetId = when (hour) {
                    in 5..11 -> settings.morningSongId
                    in 12..17 -> settings.afternoonSongId
                    else -> settings.nightSongId
                }
                allSongs.firstOrNull { it.id == targetId }
                    ?: allSongs.firstOrNull { it.id == settings.selectedSongId }
                    ?: allSongs.firstOrNull()
            }
            else -> {
                // SAME_SONG (default)
                allSongs.firstOrNull { it.id == settings.selectedSongId }
                    ?: allSongs.firstOrNull()
            }
        }
    }

    private fun buildNotification(
        song: SongItem,
        elapsedSeconds: Int,
        totalSeconds: Int
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, UnlockPlaybackService::class.java).apply {
                action = ACTION_STOP_PLAYBACK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remaining = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
        val statusText = "Unlocked • Stopping in ${remaining}s"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("UNLOCK TUNES • ${song.title}")
            .setContentText("${song.artist} | $statusText")
            .setSubText("Soundtrack Active")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(totalSeconds, elapsedSeconds, false)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Soundtrack",
                stopIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun handlePlaybackFinished() {
        notificationJob?.cancel()
        notificationJob = null
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleStop() {
        playerManager.stopPlayback()
        handlePlaybackFinished()
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Unlock Soundtrack Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active soundtrack countdown and stop controls on phone unlock"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
