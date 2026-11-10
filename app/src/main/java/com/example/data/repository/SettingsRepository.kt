package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SongItem
import com.example.data.model.UnlockSettings
import com.example.data.preset.PresetAudioGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unlock_tunes_prefs")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val AUTO_PLAY_ENABLED = booleanPreferencesKey("auto_play_enabled")
        val PLAYBACK_DURATION_SECONDS = intPreferencesKey("playback_duration_seconds")
        val VOLUME = floatPreferencesKey("volume")
        val FADE_IN_ENABLED = booleanPreferencesKey("fade_in_enabled")
        val FADE_IN_DURATION_SECONDS = intPreferencesKey("fade_in_duration_seconds")
        val FADE_OUT_ENABLED = booleanPreferencesKey("fade_out_enabled")
        val FADE_OUT_DURATION_SECONDS = intPreferencesKey("fade_out_duration_seconds")
        val SILENT_MODE_BEHAVIOR = stringPreferencesKey("silent_mode_behavior")
        val DND_BEHAVIOR = stringPreferencesKey("dnd_behavior")
        val HEADPHONE_PREFERENCE = stringPreferencesKey("headphone_preference")
        val AUDIO_FOCUS_BEHAVIOR = stringPreferencesKey("audio_focus_behavior")
        val PLAY_MODE = stringPreferencesKey("play_mode")
        val SELECTED_SONG_ID = stringPreferencesKey("selected_song_id")
        val MORNING_SONG_ID = stringPreferencesKey("morning_song_id")
        val AFTERNOON_SONG_ID = stringPreferencesKey("afternoon_song_id")
        val NIGHT_SONG_ID = stringPreferencesKey("night_song_id")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val TOTAL_UNLOCKS_TRIGGERED = intPreferencesKey("total_unlocks_triggered")
        val LAST_UNLOCK_TIMESTAMP = longPreferencesKey("last_unlock_timestamp")
        val CUSTOM_SONGS_JSON = stringPreferencesKey("custom_songs_json")
    }

    val settingsFlow: Flow<UnlockSettings> = context.dataStore.data.map { preferences ->
        UnlockSettings(
            autoPlayEnabled = preferences[PreferencesKeys.AUTO_PLAY_ENABLED] ?: true,
            playbackDurationSeconds = preferences[PreferencesKeys.PLAYBACK_DURATION_SECONDS] ?: 30,
            volume = preferences[PreferencesKeys.VOLUME] ?: 1.0f,
            fadeInEnabled = preferences[PreferencesKeys.FADE_IN_ENABLED] ?: true,
            fadeInDurationSeconds = preferences[PreferencesKeys.FADE_IN_DURATION_SECONDS] ?: 2,
            fadeOutEnabled = preferences[PreferencesKeys.FADE_OUT_ENABLED] ?: true,
            fadeOutDurationSeconds = preferences[PreferencesKeys.FADE_OUT_DURATION_SECONDS] ?: 3,
            silentModeBehavior = preferences[PreferencesKeys.SILENT_MODE_BEHAVIOR] ?: "RESPECT_SILENT",
            dndBehavior = preferences[PreferencesKeys.DND_BEHAVIOR] ?: "RESPECT_DND",
            headphonePreference = preferences[PreferencesKeys.HEADPHONE_PREFERENCE] ?: "PLAY_ANYTIME",
            audioFocusBehavior = preferences[PreferencesKeys.AUDIO_FOCUS_BEHAVIOR] ?: "PAUSE_ON_OTHER_AUDIO",
            playMode = preferences[PreferencesKeys.PLAY_MODE] ?: "SAME_SONG",
            selectedSongId = preferences[PreferencesKeys.SELECTED_SONG_ID] ?: "preset_neon_drive",
            morningSongId = preferences[PreferencesKeys.MORNING_SONG_ID] ?: "preset_morning_aura",
            afternoonSongId = preferences[PreferencesKeys.AFTERNOON_SONG_ID] ?: "preset_electric_horizon",
            nightSongId = preferences[PreferencesKeys.NIGHT_SONG_ID] ?: "preset_midnight_velocity",
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
            totalUnlocksTriggered = preferences[PreferencesKeys.TOTAL_UNLOCKS_TRIGGERED] ?: 0,
            lastUnlockTimestamp = preferences[PreferencesKeys.LAST_UNLOCK_TIMESTAMP] ?: 0L
        )
    }

    suspend fun getSettingsSnapshot(): UnlockSettings = settingsFlow.first()

    suspend fun updateAutoPlay(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_PLAY_ENABLED] = enabled }
    }

    suspend fun updateDuration(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.PLAYBACK_DURATION_SECONDS] = seconds }
    }

    suspend fun updateVolume(volume: Float) {
        context.dataStore.edit { it[PreferencesKeys.VOLUME] = volume }
    }

    suspend fun updateFadeIn(enabled: Boolean, seconds: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.FADE_IN_ENABLED] = enabled
            it[PreferencesKeys.FADE_IN_DURATION_SECONDS] = seconds
        }
    }

    suspend fun updateFadeOut(enabled: Boolean, seconds: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.FADE_OUT_ENABLED] = enabled
            it[PreferencesKeys.FADE_OUT_DURATION_SECONDS] = seconds
        }
    }

    suspend fun updateSilentModeBehavior(behavior: String) {
        context.dataStore.edit { it[PreferencesKeys.SILENT_MODE_BEHAVIOR] = behavior }
    }

    suspend fun updateDndBehavior(behavior: String) {
        context.dataStore.edit { it[PreferencesKeys.DND_BEHAVIOR] = behavior }
    }

    suspend fun updateHeadphonePreference(pref: String) {
        context.dataStore.edit { it[PreferencesKeys.HEADPHONE_PREFERENCE] = pref }
    }

    suspend fun updateAudioFocusBehavior(behavior: String) {
        context.dataStore.edit { it[PreferencesKeys.AUDIO_FOCUS_BEHAVIOR] = behavior }
    }

    suspend fun updatePlayMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.PLAY_MODE] = mode }
    }

    suspend fun setSelectedSong(songId: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_SONG_ID] = songId }
    }

    suspend fun setTimeSlotSong(slot: String, songId: String) {
        context.dataStore.edit {
            when (slot.uppercase()) {
                "MORNING" -> it[PreferencesKeys.MORNING_SONG_ID] = songId
                "AFTERNOON" -> it[PreferencesKeys.AFTERNOON_SONG_ID] = songId
                "NIGHT" -> it[PreferencesKeys.NIGHT_SONG_ID] = songId
            }
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { it[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    suspend fun recordUnlockTriggered() {
        context.dataStore.edit {
            val count = it[PreferencesKeys.TOTAL_UNLOCKS_TRIGGERED] ?: 0
            it[PreferencesKeys.TOTAL_UNLOCKS_TRIGGERED] = count + 1
            it[PreferencesKeys.LAST_UNLOCK_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit {
            it[PreferencesKeys.AUTO_PLAY_ENABLED] = true
            it[PreferencesKeys.PLAYBACK_DURATION_SECONDS] = 30
            it[PreferencesKeys.VOLUME] = 1.0f
            it[PreferencesKeys.FADE_IN_ENABLED] = true
            it[PreferencesKeys.FADE_IN_DURATION_SECONDS] = 2
            it[PreferencesKeys.FADE_OUT_ENABLED] = true
            it[PreferencesKeys.FADE_OUT_DURATION_SECONDS] = 3
            it[PreferencesKeys.SILENT_MODE_BEHAVIOR] = "RESPECT_SILENT"
            it[PreferencesKeys.DND_BEHAVIOR] = "RESPECT_DND"
            it[PreferencesKeys.HEADPHONE_PREFERENCE] = "PLAY_ANYTIME"
            it[PreferencesKeys.AUDIO_FOCUS_BEHAVIOR] = "PAUSE_ON_OTHER_AUDIO"
            it[PreferencesKeys.PLAY_MODE] = "SAME_SONG"
            it[PreferencesKeys.SELECTED_SONG_ID] = "preset_neon_drive"
        }
    }

    val customSongsFlow: Flow<List<SongItem>> = context.dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.CUSTOM_SONGS_JSON] ?: "[]"
        parseSongsFromJson(json)
    }

    suspend fun getCustomSongs(): List<SongItem> = customSongsFlow.first()

    suspend fun addCustomSong(song: SongItem) {
        val current = getCustomSongs().toMutableList()
        current.removeAll { it.id == song.id || it.uriString == song.uriString }
        current.add(0, song)
        saveCustomSongs(current)
        // Automatically select as active song
        setSelectedSong(song.id)
    }

    suspend fun removeCustomSong(songId: String) {
        val current = getCustomSongs().toMutableList()
        current.removeAll { it.id == songId }
        saveCustomSongs(current)
        val currentSelected = getSettingsSnapshot().selectedSongId
        if (currentSelected == songId) {
            setSelectedSong("preset_neon_drive")
        }
    }

    private suspend fun saveCustomSongs(songs: List<SongItem>) {
        val array = JSONArray()
        songs.forEach { song ->
            val obj = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("durationMs", song.durationMs)
                put("uriString", song.uriString)
                put("isPreset", song.isPreset)
                put("presetFileName", song.presetFileName ?: "")
                put("albumArtUri", song.albumArtUri ?: "")
                put("genre", song.genre)
                put("timeSlot", song.timeSlot ?: "")
            }
            array.put(obj)
        }
        context.dataStore.edit {
            it[PreferencesKeys.CUSTOM_SONGS_JSON] = array.toString()
        }
    }

    private fun parseSongsFromJson(json: String): List<SongItem> {
        val list = mutableListOf<SongItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SongItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        artist = obj.optString("artist", "Unknown Artist"),
                        durationMs = obj.optLong("durationMs", 30000L),
                        uriString = obj.getString("uriString"),
                        isPreset = obj.optBoolean("isPreset", false),
                        presetFileName = obj.optString("presetFileName").takeIf { it.isNotEmpty() },
                        albumArtUri = obj.optString("albumArtUri").takeIf { it.isNotEmpty() },
                        genre = obj.optString("genre", "Audio File"),
                        timeSlot = obj.optString("timeSlot").takeIf { it.isNotEmpty() }
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    suspend fun getAllSongs(): List<SongItem> {
        val presets = PresetAudioGenerator.ensurePresetsGenerated(context)
        val custom = getCustomSongs()
        return custom + presets
    }

    suspend fun getSongById(id: String?): SongItem? {
        if (id == null) return null
        return getAllSongs().firstOrNull { it.id == id }
    }
}
