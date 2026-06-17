package com.jhc.volumetile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class VolumePanelForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var foregroundStarted = false

    private val stopRunnable = Runnable {
        stopForegroundCompat()
        foregroundStarted = false
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(stopRunnable)

        try {
            if (!foregroundStarted) {
                startAsForegroundService()
                foregroundStarted = true
            }

            // Call immediately after startForeground(). The old build added a 60 ms delay here.
            showVolumePanel()
        } catch (_: Throwable) {
            // Keep the service from crashing if audio hardening is set to throw.
        } finally {
            // Keep the foreground service warm briefly. Repeated taps in this window avoid
            // the cold start/startForeground path and only re-send the volume-panel request.
            handler.postDelayed(stopRunnable, KEEP_ALIVE_MS)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun showVolumePanel() {
        getSystemService(AudioManager::class.java).adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun startAsForegroundService() {
        createNotificationChannelIfNeeded()
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        builder
            .setSmallIcon(R.drawable.ic_audiotrack_black_24dp)
            .setContentTitle(getString(R.string.volume_tile_name))
            .setContentText("")
            .setShowWhen(false)
            .setOngoing(false)
            .setLocalOnly(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(Notification.PRIORITY_MIN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Ask SystemUI not to show the short-lived foreground-service notification immediately.
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFERRED)
        }

        return builder.build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.volume_tile_name),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun stopForegroundCompat() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Throwable) {
            // Nothing else to do.
        }
    }

    private companion object {
        private const val CHANNEL_ID = "volume_panel_service"
        private const val NOTIFICATION_ID = 1001
        private const val KEEP_ALIVE_MS = 2_500L
    }
}
