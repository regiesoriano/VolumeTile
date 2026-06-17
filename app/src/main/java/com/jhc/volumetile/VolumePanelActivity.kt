package com.jhc.volumetile

import android.app.Activity
import android.media.AudioManager
import android.os.Bundle

class VolumePanelActivity : Activity() {
    private var alreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDimAmount(0f)
    }

    override fun onResume() {
        super.onResume()

        if (alreadyShown) {
            return
        }
        alreadyShown = true

        window.decorView.postDelayed({
            try {
                getSystemService(AudioManager::class.java).adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
            } catch (_: Throwable) {
                // Keep the tile from crashing if a test build enables audio hardening exceptions.
            } finally {
                window.decorView.postDelayed({
                    finishAndRemoveTask()
                }, 500)
            }
        }, 120)
    }
}
