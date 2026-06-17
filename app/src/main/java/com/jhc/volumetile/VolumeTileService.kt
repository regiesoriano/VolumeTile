package com.jhc.volumetile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService

class VolumeTileService : TileService() {
    override fun onClick() {
        if (isLocked) {
            unlockAndRun { launchNoDisplayVolumePanelActivity() }
        } else {
            launchNoDisplayVolumePanelActivity()
        }
    }

    private fun launchNoDisplayVolumePanelActivity() {
        val intent = Intent(this, VolumePanelActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
