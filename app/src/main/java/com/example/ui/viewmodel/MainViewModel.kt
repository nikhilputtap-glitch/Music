package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.UnlockAudioPlayerManager
import com.example.data.model.SongItem
import com.example.data.model.UnlockSettings
import com.example.data.repository.SettingsRepository
import com.example.service.UnlockPlaybackService
import com.example.ui.components.MonitoringStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val playerManager = UnlockAudioPlayerManager.getInstance(application)

    val settings: StateFlow<UnlockSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UnlockSettings()
    )

    val playbackState: StateFlow<AudioPlaybackState> = playerManager.playbackState

    private val _allSongs = MutableStateFlow<List<SongItem>>(emptyList())
    val allSongs: StateFlow<List<SongItem>> = _allSongs.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    init {
        refreshSongs()
        // Ensure monitoring service is running in background if auto-play is enabled
        viewModelScope.launch {
            val current = repository.getSettingsSnapshot()
            if (current.autoPlayEnabled) {
                UnlockPlaybackService.startMonitoring(getApplication())
            }
        }
    }

    fun clearNotificationMessage() {
        _notificationMessage.value = null
    }

    fun refreshSongs() {
        viewModelScope.launch {
            val list = repository.getAllSongs()
            _allSongs.value = list
        }
    }

    val selectedSong: StateFlow<SongItem?> = combine(settings, allSongs) { currentSettings, songs ->
        songs.firstOrNull { it.id == currentSettings.selectedSongId } ?: songs.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val monitoringStatus: StateFlow<MonitoringStatus> = combine(settings, selectedSong) { currentSettings, song ->
        when {
            !currentSettings.autoPlayEnabled -> MonitoringStatus.PAUSED
            song == null -> MonitoringStatus.SONG_NOT_SELECTED
            else -> MonitoringStatus.ACTIVE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonitoringStatus.ACTIVE
    )

    fun toggleAutoPlay() {
        viewModelScope.launch {
            val current = settings.value.autoPlayEnabled
            val newState = !current
            repository.updateAutoPlay(newState)
            val app = getApplication<Application>()
            if (newState) {
                UnlockPlaybackService.startMonitoring(app)
                _notificationMessage.value = "Auto-Play Activated! Song will play every time you unlock your phone."
            } else {
                UnlockPlaybackService.stopMonitoring(app)
                _notificationMessage.value = "Auto-Play Paused."
            }
        }
    }

    /**
     * Sets a song as the active unlock soundtrack and starts background monitoring.
     */
    fun activateSongForUnlock(songId: String) {
        viewModelScope.launch {
            repository.setSelectedSong(songId)
            repository.updateAutoPlay(true)
            val app = getApplication<Application>()
            UnlockPlaybackService.startMonitoring(app)

            val song = repository.getSongById(songId)
            val title = song?.title ?: "Track"
            _notificationMessage.value = "✓ '$title' is set for Unlock Screen! Lock your phone and unlock to hear it."
        }
    }

    fun setDuration(seconds: Int) {
        viewModelScope.launch {
            repository.updateDuration(seconds)
            _notificationMessage.value = "Duration set to ${seconds}s"
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            repository.updateVolume(volume)
        }
    }

    fun setFadeIn(enabled: Boolean, seconds: Int) {
        viewModelScope.launch {
            repository.updateFadeIn(enabled, seconds)
        }
    }

    fun setFadeOut(enabled: Boolean, seconds: Int) {
        viewModelScope.launch {
            repository.updateFadeOut(enabled, seconds)
        }
    }

    fun setSilentModeBehavior(behavior: String) {
        viewModelScope.launch {
            repository.updateSilentModeBehavior(behavior)
        }
    }

    fun setDndBehavior(behavior: String) {
        viewModelScope.launch {
            repository.updateDndBehavior(behavior)
        }
    }

    fun setHeadphonePreference(pref: String) {
        viewModelScope.launch {
            repository.updateHeadphonePreference(pref)
        }
    }

    fun setAudioFocusBehavior(behavior: String) {
        viewModelScope.launch {
            repository.updateAudioFocusBehavior(behavior)
        }
    }

    fun setPlayMode(mode: String) {
        viewModelScope.launch {
            repository.updatePlayMode(mode)
        }
    }

    fun selectSong(songId: String) {
        viewModelScope.launch {
            repository.setSelectedSong(songId)
        }
    }

    fun setTimeSlotSong(slot: String, songId: String) {
        viewModelScope.launch {
            repository.setTimeSlotSong(slot, songId)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            val app = getApplication<Application>()
            UnlockPlaybackService.startMonitoring(app)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            repository.resetToDefaults()
            val app = getApplication<Application>()
            UnlockPlaybackService.startMonitoring(app)
            _notificationMessage.value = "Settings reset to defaults."
        }
    }

    fun importAudioUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            var title = "Custom Soundtrack"
            var artist = "Local Storage"
            var durationMs = 30000L

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                    if (it.isNotBlank()) title = it
                }
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                    if (it.isNotBlank()) artist = it
                }
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                    if (it > 0) durationMs = it
                }
                retriever.release()
            } catch (e: Exception) {
                uri.lastPathSegment?.let { seg ->
                    val clean = seg.substringAfterLast("/").substringAfterLast(":")
                    if (clean.isNotBlank()) title = clean
                }
            }

            val newSong = SongItem(
                id = "custom_" + System.currentTimeMillis(),
                title = title,
                artist = artist,
                durationMs = durationMs,
                uriString = uri.toString(),
                isPreset = false,
                genre = "Custom Audio"
            )

            repository.addCustomSong(newSong)
            refreshSongs()

            // Directly activate for unlock screen!
            activateSongForUnlock(newSong.id)
        }
    }

    fun removeSong(songId: String) {
        viewModelScope.launch {
            repository.removeCustomSong(songId)
            refreshSongs()
        }
    }

    fun playTestSoundtrack() {
        val app = getApplication<Application>()
        UnlockPlaybackService.startPlayback(app, isTest = true)
    }

    fun stopPlayback() {
        val app = getApplication<Application>()
        UnlockPlaybackService.stopPlayback(app)
    }
}
