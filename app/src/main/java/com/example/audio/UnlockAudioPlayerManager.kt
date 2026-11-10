package com.example.audio

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.example.data.model.SongItem
import com.example.data.model.UnlockSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val song: SongItem? = null,
    val elapsedSeconds: Int = 0,
    val totalDurationSeconds: Int = 30,
    val currentVolume: Float = 1.0f,
    val statusMessage: String = "Monitoring Active"
)

class UnlockAudioPlayerManager private constructor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaPlayer: MediaPlayer? = null
    private var countdownJob: Job? = null
    private var fadeJob: Job? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var onPlaybackFinishedListener: (() -> Unit)? = null

    fun setOnPlaybackFinishedListener(listener: () -> Unit) {
        onPlaybackFinishedListener = listener
    }

    /**
     * Checks if playback should be permitted based on user preferences and current device state.
     */
    fun shouldAllowPlayback(settings: UnlockSettings): Pair<Boolean, String> {
        if (!settings.autoPlayEnabled) {
            return false to "Auto Play is disabled"
        }

        // Silent / Vibrate mode check
        val ringerMode = audioManager.ringerMode
        val isSilentOrVibrate = ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE

        if (isSilentOrVibrate && settings.silentModeBehavior == "RESPECT_SILENT") {
            return false to "Phone is in Silent/Vibrate mode"
        }

        // Do Not Disturb check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = notificationManager.currentInterruptionFilter
            val isDndActive = filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                    filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                    filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY

            if (isDndActive && settings.dndBehavior == "RESPECT_DND") {
                return false to "Do Not Disturb is active"
            }
        }

        // Headphone check
        val isHeadphoneConnected = isHeadsetConnected()
        if (settings.headphonePreference == "ONLY_HEADPHONES" && !isHeadphoneConnected) {
            return false to "Skipped: Headphones not connected"
        }
        if (settings.headphonePreference == "SKIP_IF_DISCONNECTED" && !isHeadphoneConnected) {
            return false to "Skipped: Headphones disconnected"
        }

        return true to "Ready"
    }

    fun isHeadsetConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> return true
                }
            }
            return false
        } else {
            @Suppress("DEPRECATION")
            return audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
        }
    }

    /**
     * Starts playback for the specified song and settings.
     */
    fun playSong(
        song: SongItem,
        settings: UnlockSettings,
        onFinished: (() -> Unit)? = null
    ) {
        stopPlayback()

        onFinished?.let { onPlaybackFinishedListener = it }

        val targetDuration = settings.playbackDurationSeconds.coerceIn(5, 300)
        val targetVolume = settings.volume.coerceIn(0.05f, 1.0f)
        val initialVolume = if (settings.fadeInEnabled) 0.05f else targetVolume

        try {
            requestAudioFocus(settings)

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, Uri.parse(song.uriString))
                isLooping = true
                setVolume(initialVolume, initialVolume)
                prepare()
                start()
            }
            mediaPlayer = player

            _playbackState.update {
                AudioPlaybackState(
                    isPlaying = true,
                    song = song,
                    elapsedSeconds = 0,
                    totalDurationSeconds = targetDuration,
                    currentVolume = initialVolume,
                    statusMessage = "Playing ${song.title}"
                )
            }

            // Handle Fade In
            if (settings.fadeInEnabled && settings.fadeInDurationSeconds > 0) {
                startFade(
                    startVol = 0.05f,
                    endVol = targetVolume,
                    durationMs = (settings.fadeInDurationSeconds * 1000L).coerceAtMost((targetDuration * 500L))
                )
            }

            // Accurate elapsed time countdown loop
            startCountdown(targetDuration, settings, targetVolume)

        } catch (e: Exception) {
            e.printStackTrace()
            stopPlayback()
            _playbackState.update {
                it.copy(
                    isPlaying = false,
                    statusMessage = "Error playing audio: ${e.localizedMessage ?: "Unknown"}"
                )
            }
        }
    }

    private fun startCountdown(
        targetDuration: Int,
        settings: UnlockSettings,
        targetVolume: Float
    ) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            val startRealtime = SystemClock.elapsedRealtime()
            val totalMs = targetDuration * 1000L
            val fadeOutMs = if (settings.fadeOutEnabled) (settings.fadeOutDurationSeconds * 1000L) else 0L

            var hasStartedFadeOut = false

            while (isActive) {
                val elapsedMs = SystemClock.elapsedRealtime() - startRealtime
                val elapsedSec = (elapsedMs / 1000L).toInt()
                val remainingMs = totalMs - elapsedMs

                if (elapsedMs >= totalMs) {
                    // Time is up!
                    stopPlayback()
                    onPlaybackFinishedListener?.invoke()
                    break
                }

                // Trigger fade out when approaching completion
                if (settings.fadeOutEnabled && fadeOutMs > 0 && remainingMs <= fadeOutMs && !hasStartedFadeOut) {
                    hasStartedFadeOut = true
                    startFade(
                        startVol = _playbackState.value.currentVolume,
                        endVol = 0.01f,
                        durationMs = remainingMs
                    )
                }

                _playbackState.update {
                    it.copy(
                        elapsedSeconds = elapsedSec.coerceAtMost(targetDuration)
                    )
                }

                delay(100)
            }
        }
    }

    private fun startFade(startVol: Float, endVol: Float, durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val steps = 25
            val stepDelay = (durationMs / steps).coerceAtLeast(16L)
            val volDelta = (endVol - startVol) / steps

            var currentVol = startVol
            for (i in 0 until steps) {
                if (!isActive || mediaPlayer == null) break
                currentVol = (currentVol + volDelta).coerceIn(0.0f, 1.0f)
                try {
                    mediaPlayer?.setVolume(currentVol, currentVol)
                    _playbackState.update { it.copy(currentVolume = currentVol) }
                } catch (e: Exception) {
                    break
                }
                delay(stepDelay)
            }
        }
    }

    private fun requestAudioFocus(settings: UnlockSettings) {
        val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (settings.audioFocusBehavior == "PAUSE_ON_OTHER_AUDIO") {
                        stopPlayback()
                    } else if (settings.audioFocusBehavior == "DUCK_VOLUME") {
                        val ducked = (_playbackState.value.currentVolume * 0.3f).coerceAtLeast(0.05f)
                        mediaPlayer?.setVolume(ducked, ducked)
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    if (settings.audioFocusBehavior != "PLAY_OVER") {
                        val ducked = (_playbackState.value.currentVolume * 0.3f).coerceAtLeast(0.05f)
                        mediaPlayer?.setVolume(ducked, ducked)
                    }
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    val full = settings.volume
                    mediaPlayer?.setVolume(full, full)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    fun stopPlayback() {
        countdownJob?.cancel()
        countdownJob = null
        fadeJob?.cancel()
        fadeJob = null

        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        abandonAudioFocus()

        _playbackState.update {
            it.copy(
                isPlaying = false,
                elapsedSeconds = 0,
                statusMessage = "Monitoring Active"
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    companion object {
        @Volatile
        private var instance: UnlockAudioPlayerManager? = null

        fun getInstance(context: Context): UnlockAudioPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: UnlockAudioPlayerManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
