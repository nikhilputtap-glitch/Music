package com.example.data.model

data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uriString: String,
    val isPreset: Boolean = false,
    val presetFileName: String? = null,
    val albumArtUri: String? = null,
    val genre: String = "Electronic",
    val timeSlot: String? = null // "MORNING", "AFTERNOON", "NIGHT", null
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
