package com.example.data.preset

import android.content.Context
import android.net.Uri
import com.example.data.model.SongItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object PresetAudioGenerator {

    private const val SAMPLE_RATE = 22050
    private const val NUM_CHANNELS = 1
    private const val BITS_PER_SAMPLE = 16

    data class PresetMeta(
        val id: String,
        val title: String,
        val artist: String,
        val genre: String,
        val fileName: String,
        val baseFrequency: Double,
        val chords: List<Double>,
        val bpm: Double,
        val durationSeconds: Int = 32
    )

    private val PRESETS = listOf(
        PresetMeta(
            id = "preset_neon_drive",
            title = "Cyberpunk Neon Drive",
            artist = "Unlock Tunes Synth",
            genre = "Synthwave",
            fileName = "preset_neon_drive.wav",
            baseFrequency = 130.81, // C3
            chords = listOf(130.81, 155.56, 174.61, 196.00), // C, Eb, F, G
            bpm = 120.0,
            durationSeconds = 35
        ),
        PresetMeta(
            id = "preset_cosmic_awakening",
            title = "Cosmic Awakening",
            artist = "Aura Sound Lab",
            genre = "Ambient Chill",
            fileName = "preset_cosmic_awakening.wav",
            baseFrequency = 220.0, // A3
            chords = listOf(220.0, 261.63, 329.63, 440.0), // A minor 7
            bpm = 85.0,
            durationSeconds = 35
        ),
        PresetMeta(
            id = "preset_electric_horizon",
            title = "Electric Horizon",
            artist = "Vector Echoes",
            genre = "Electronic",
            fileName = "preset_electric_horizon.wav",
            baseFrequency = 146.83, // D3
            chords = listOf(146.83, 174.61, 220.0, 293.66), // D minor
            bpm = 128.0,
            durationSeconds = 35
        ),
        PresetMeta(
            id = "preset_morning_aura",
            title = "Morning Aura",
            artist = "Dawn Harmonic",
            genre = "Acoustic Synth",
            fileName = "preset_morning_aura.wav",
            baseFrequency = 261.63, // C4
            chords = listOf(261.63, 329.63, 392.0, 523.25), // C Major
            bpm = 95.0,
            durationSeconds = 35
        ),
        PresetMeta(
            id = "preset_midnight_velocity",
            title = "Midnight Velocity",
            artist = "Night Pulse",
            genre = "Dark Electro",
            fileName = "preset_midnight_velocity.wav",
            baseFrequency = 110.0, // A2
            chords = listOf(110.0, 138.59, 164.81, 220.0), // A Major / Dark
            bpm = 132.0,
            durationSeconds = 35
        )
    )

    suspend fun ensurePresetsGenerated(context: Context): List<SongItem> = withContext(Dispatchers.IO) {
        val presetDir = File(context.filesDir, "presets").apply {
            if (!exists()) mkdirs()
        }

        PRESETS.map { meta ->
            val wavFile = File(presetDir, meta.fileName)
            if (!wavFile.exists() || wavFile.length() < 1000) {
                generateWavFile(wavFile, meta)
            }
            SongItem(
                id = meta.id,
                title = meta.title,
                artist = meta.artist,
                durationMs = meta.durationSeconds * 1000L,
                uriString = Uri.fromFile(wavFile).toString(),
                isPreset = true,
                presetFileName = meta.fileName,
                genre = meta.genre
            )
        }
    }

    private fun generateWavFile(file: File, meta: PresetMeta) {
        val totalSamples = SAMPLE_RATE * meta.durationSeconds
        val pcmData = ShortArray(totalSamples)

        val beatDurationSamples = (SAMPLE_RATE * 60.0 / meta.bpm).toInt()
        val chords = meta.chords

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val chordIndex = ((i / (beatDurationSamples * 2)) % chords.size)
            val currentFreq = chords[chordIndex]

            // Main synth tone with harmonics
            val sine1 = sin(2.0 * Math.PI * currentFreq * t)
            val sine2 = 0.5 * sin(2.0 * Math.PI * (currentFreq * 2.0) * t)
            val sineSub = 0.6 * sin(2.0 * Math.PI * (meta.baseFrequency) * t)

            // Beat pulse / envelope
            val beatPhase = (i % beatDurationSamples).toDouble() / beatDurationSamples
            val beatEnv = Math.exp(-3.0 * beatPhase)

            // Soft master fade in / out
            val envelope = when {
                t < 1.0 -> t
                t > (meta.durationSeconds - 2.0) -> (meta.durationSeconds - t) / 2.0
                else -> 1.0
            }.coerceIn(0.0, 1.0)

            val sampleVal = ((sine1 * 0.4 + sine2 * 0.2 + sineSub * 0.4 * beatEnv) * envelope * 0.7)
            val clamped = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            pcmData[i] = clamped.toShort()
        }

        writeWav(file, pcmData)
    }

    private fun writeWav(file: File, pcmData: ShortArray) {
        val byteDataLength = pcmData.size * 2
        val totalDataLength = byteDataLength + 36

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(totalDataLength)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size for PCM
                putShort(1.toShort()) // AudioFormat 1 = PCM
                putShort(NUM_CHANNELS.toShort())
                putInt(SAMPLE_RATE)
                putInt(SAMPLE_RATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8)) // ByteRate
                putShort((NUM_CHANNELS * (BITS_PER_SAMPLE / 8)).toShort()) // BlockAlign
                putShort(BITS_PER_SAMPLE.toShort())
                put("data".toByteArray())
                putInt(byteDataLength)
            }
            out.write(header.array())

            val buffer = ByteBuffer.allocate(pcmData.size * 2).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                for (s in pcmData) {
                    putShort(s)
                }
            }
            out.write(buffer.array())
            out.flush()
        }
    }
}
