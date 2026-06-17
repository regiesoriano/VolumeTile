package com.jhc.volumetile

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Space

class VolumeTileService : TileService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onClick() {
        if (isLocked) {
            unlockAndRun { showVolumePanelViaInvisibleDialog() }
        } else {
            showVolumePanelViaInvisibleDialog()
        }
    }

    private fun showVolumePanelViaInvisibleDialog() {
        try {
            val dialog = Dialog(this, R.style.Theme_VolumeTile_InvisibleDialog)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(
                Space(this),
                ViewGroup.LayoutParams(1, 1)
            )

            configureInvisibleDialogWindow(dialog.window)

            dialog.setOnShowListener {
                configureInvisibleDialogWindow(dialog.window)
                try {
                    showVolumePanel()
                } catch (_: Throwable) {
                    // Keep the tile from crashing if audio hardening is set to throw.
                }

                handler.postDelayed({
                    try {
                        if (dialog.isShowing) dialog.dismiss()
                    } catch (_: Throwable) {
                        // Nothing else to do.
                    }
                }, DISMISS_DELAY_MS)
            }

            // showDialog() is a TileService API that also collapses Quick Settings.
            showDialog(dialog)
        } catch (_: Throwable) {
            // Final fallback: this is likely blocked on Android 17, but it is harmless to try.
            try {
                showVolumePanel()
            } catch (_: Throwable) {
                // Nothing else to do.
            }
        }
    }

    private fun configureInvisibleDialogWindow(window: Window?) {
        if (window == null) return

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        val layoutParams = window.attributes
        layoutParams.width = 1
        layoutParams.height = 1
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.dimAmount = 0f
        layoutParams.alpha = 0.01f
        window.attributes = layoutParams
    }

    private fun showVolumePanel() {
        getSystemService(AudioManager::class.java).adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private companion object {
        private const val DISMISS_DELAY_MS = 180L
    }
}
