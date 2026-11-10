package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class UnlockPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var playerManager: UnlockAudioPlayerManager
    private lateinit var settingsRepository: SettingsRepository
    private var wakeLock: PowerManager.WakeLock? = null
    private var notificationJob: Job? = null
    private var isReceiverRegistered = false

    private val screenUnlockReceiver = object : BroadcastReceiver() {
        private var lastUnlockTime = 0L
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastUnlockTime > 2500L) {
                    lastUnlockTime = now
                    handleStartPlayback(isTest = false)
                }
            }
        }
    }

    companion object {
        const val ACTION_START_MONITORING = "com.nikhil.unlocktunes.START_MONITORING"
        const val ACTION_START_UNLOCK_PLAYBACK = "com.nikhil.unlocktunes.START_PLAYBACK"
        const val ACTION_STOP_PLAYBACK = "com.nikhil.unlocktunes.STOP_PLAYBACK"
        const val ACTION_TEST_PLAYBACK = "com.nikhil.unlocktunes.TEST_PLAYBACK"
        const val ACTION_STOP_SERVICE = "com.nikhil.unlocktunes.STOP_SERVICE"

        const val CHANNEL_ID = "unlock_tunes_playback_channel"
        const val NOTIFICATION_ID = 1001

        fun startMonitoring(context: Context) {
            val intent = Intent(context, UnlockPlaybackService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            startServiceCompat(context, intent)
        }

        fun startPlayback(context: Context, isTest: Boolean = false) {
            val intent = Intent(context, UnlockPlaybackService::class.java).apply {
                action = if (isTest) ACTION_TEST_PLAYBACK else ACTION_START_UNLOCK_PLAYBACK
            }
            startServiceCompat(context, intent)
        }

        fun stopPlayback(context: Context) {
            val intent = Intent(context, UnlockPlaybackService::class.java).apply {
                action = ACTION_STOP_PLAYBACK
            }
            context.startService(intent)
        }

        fun stopMonitoring(context: Context) {
            val intent = Intent(context, UnlockPlaybackService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }

        private fun startServiceCompat(context: Context, intent: Intent) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

        registerUnlockReceiver()
    }

    private fun registerUnlockReceiver() {
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_USER_PRESENT)
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_SCREEN_OFF)
                }
                registerReceiver(screenUnlockReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerUnlockReceiver()

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                handleStopService()
                return START_NOT_STICKY
            }
            ACTION_STOP_PLAYBACK -> {
                handleStopPlaybackOnly()
                return START_STICKY
            }
            ACTION_TEST_PLAYBACK -> {
                handleStartPlayback(isTest = true)
                return START_STICKY
            }
            ACTION_START_UNLOCK_PLAYBACK -> {
                handleStartPlayback(isTest = false)
                return START_STICKY
            }
            ACTION_START_MONITORING -> {
                showIdleMonitoringNotification()
                return START_STICKY
            }
            else -> {
                showIdleMonitoringNotification()
                return START_STICKY
            }
        }
    }

    private fun showIdleMonitoringNotification() {
        serviceScope.launch {
            val settings = settingsRepository.getSettingsSnapshot()
            val song = pickSong(settings)
            val songName = song?.title ?: "Preset Soundtrack"

            val notification = buildIdleNotification(songName)
            startInForeground(notification)
        }
    }

    private fun handleStartPlayback(isTest: Boolean) {
        serviceScope.launch {
            val settings = settingsRepository.getSettingsSnapshot()

            if (!isTest) {
                val (allowed, _) = playerManager.shouldAllowPlayback(settings)
                if (!allowed) {
                    showIdleMonitoringNotification()
                    return@launch
                }
            }

            val songToPlay = pickSong(settings)
            if (songToPlay == null) {
                showIdleMonitoringNotification()
                return@launch
            }

            // Acquire wakelock safely
            try {
                wakeLock?.acquire((settings.playbackDurationSeconds + 5) * 1000L)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val playingNotification = buildPlayingNotification(songToPlay, 0, settings.playbackDurationSeconds)
            startInForeground(playingNotification)

            if (!isTest) {
                settingsRepository.recordUnlockTriggered()
            }

            playerManager.playSong(songToPlay, settings) {
                handlePlaybackFinished()
            }

            observePlaybackProgress(songToPlay)
        }
    }

    private fun startInForeground(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                    val updatedNotification = buildPlayingNotification(
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
            "RANDOM_SONG" -> allSongs.randomOrNull() ?: allSongs.firstOrNull()
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
                allSongs.firstOrNull { it.id == settings.selectedSongId } ?: allSongs.firstOrNull()
            }
        }
    }

    private fun buildIdleNotification(songTitle: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val testIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, UnlockPlaybackService::class.java).apply {
                action = ACTION_TEST_PLAYBACK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("UNLOCK TUNES • Monitoring Active")
            .setContentText("Ready • Will play '$songTitle' on next unlock")
            .setSubText("Soundtrack Active")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_play,
                "Test Soundtrack",
                testIntent
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildPlayingNotification(
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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("UNLOCK TUNES • ${song.title}")
            .setContentText("${song.artist} • Stopping in ${remaining}s")
            .setSubText("Playing Unlock Soundtrack")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(totalSeconds, elapsedSeconds, false)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
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
        // Stay alive in background! Transition back to idle monitoring
        showIdleMonitoringNotification()
    }

    private fun handleStopPlaybackOnly() {
        playerManager.stopPlayback()
        handlePlaybackFinished()
    }

    private fun handleStopService() {
        playerManager.stopPlayback()
        releaseWakeLock()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(screenUnlockReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {}
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Unlock Soundtrack Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors device unlocks and plays your chosen soundtrack"
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
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(screenUnlockReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {}
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
