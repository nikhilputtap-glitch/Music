package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.UnlockAudioPlayerManager
import com.example.data.model.SongItem
import com.example.data.model.UnlockSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UNLOCK TUNES", appName)
    }

    @Test
    fun `default settings test`() {
        val settings = UnlockSettings()
        assertTrue(settings.autoPlayEnabled)
        assertEquals(30, settings.playbackDurationSeconds)
        assertEquals("SAME_SONG", settings.playMode)
        assertEquals(1.0f, settings.volume, 0.01f)
        assertTrue(settings.fadeInEnabled)
        assertTrue(settings.fadeOutEnabled)
    }

    @Test
    fun `song item duration formatting`() {
        val song = SongItem(
            id = "test_1",
            title = "Test Track",
            artist = "Test Artist",
            durationMs = 95000L,
            uriString = "content://media/test"
        )
        assertEquals("1:35", song.formattedDuration)
    }

    @Test
    fun `player manager disables playback when autoplay is disabled`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = UnlockAudioPlayerManager.getInstance(context)
        val disabledSettings = UnlockSettings(autoPlayEnabled = false)
        val (allowed, reason) = manager.shouldAllowPlayback(disabledSettings)
        assertFalse(allowed)
        assertEquals("Auto Play is disabled", reason)
    }
}
