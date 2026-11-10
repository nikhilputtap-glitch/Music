package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.service.UnlockPlaybackService

class UnlockReceiver : BroadcastReceiver() {

    companion object {
        private var lastUnlockElapsedRealtime: Long = 0L
        private const val DEBOUNCE_THRESHOLD_MS = 3000L
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastUnlockElapsedRealtime < DEBOUNCE_THRESHOLD_MS) {
                // Ignore duplicate unlock event within 3 seconds
                return
            }
            lastUnlockElapsedRealtime = now

            // Start foreground playback service
            try {
                UnlockPlaybackService.startPlayback(context, isTest = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
