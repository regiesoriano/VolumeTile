# VolumeTile Android 17 no-window foreground-service patch

This variant avoids the visible transparent Activity window.

Flow:

1. QS tile click launches `VolumePanelActivity` with `startActivityAndCollapse()` so the notification shade collapses.
2. `VolumePanelActivity` uses `Theme.NoDisplay`, starts `VolumePanelForegroundService`, and immediately finishes. It should not draw an app window.
3. `VolumePanelForegroundService` briefly promotes itself to a `mediaPlayback` foreground service.
4. The service calls `AudioManager.adjustStreamVolume(STREAM_MUSIC, ADJUST_SAME, FLAG_SHOW_UI)`.
5. The service removes its notification and stops itself.

Reason:

Android 17 blocks background volume APIs unless the app has a visible Activity or a non-short foreground service. The previous patch used a visible Activity. This patch uses a no-display Activity only as a user-trigger bridge, then performs the volume UI call from a foreground service.

Tradeoff:

Android may briefly show a foreground-service chip/notification, but the app Activity window should no longer flash.
