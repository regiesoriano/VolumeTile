package com.jhc.volumetile

import android.service.quicksettings.TileService
import android.util.Log
import java.io.InputStream

class VolumeTileService : TileService() {
    override fun onClick() {
        Thread {
            runCatching {
                val process = ProcessBuilder(
                    "/system/bin/service",
                    "call",
                    "media_session",
                    "9",
                    "s16",
                    "com.android.shell",
                    "s16",
                    "com.android.shell",
                    "i32",
                    "3",
                    "i32",
                    "0",
                    "i32",
                    "1"
                ).redirectErrorStream(true).start()

                process.inputStream.use { it.drain() }
                process.waitFor()
            }.onFailure {
                Log.e(TAG, "Failed to show volume panel", it)
            }
        }.start()
    }

    private fun InputStream.drain() {
        val buffer = ByteArray(256)
        while (read(buffer) != -1) {
            // Discard output from /system/bin/service.
        }
    }

    companion object {
        private const val TAG = "VolumeTile"
    }
}
