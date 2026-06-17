package com.jhc.volumetile

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle

class VolumePanelActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val serviceIntent = Intent(this, VolumePanelForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (_: Throwable) {
            // If the foreground service cannot be started, make one last no-window attempt.
            try {
                getSystemService(AudioManager::class.java).adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
            } catch (_: Throwable) {
                // Keep the activity from crashing if audio hardening is set to throw.
            }
        } finally {
            // Theme.NoDisplay activities must finish immediately. This prevents an app window flash.
            finish()
        }
    }
}
