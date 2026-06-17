# Android 17 VolumeTile patch

This fork changes the Volume QS tile so that it no longer calls `AudioManager.adjustStreamVolume()` directly from the background `TileService`.

Android 17 blocks background volume APIs unless the app has a visible activity or a valid foreground service. The original tile called:

```kotlin
adjustStreamVolume(STREAM_MUSIC, ADJUST_SAME, FLAG_SHOW_UI)
```

directly inside `VolumeTileService.onClick()`, which can be silently ignored on Android 17.

The patched flow is:

```text
QS tile click -> transparent VolumePanelActivity -> ADJUST_SAME + FLAG_SHOW_UI -> activity closes
```

This keeps the compact Android volume overlay behavior without changing the media volume.

## Build with GitHub Actions

1. Push this source to your fork.
2. Open the repository's Actions tab.
3. Run the `Build APK` workflow.
4. Download the `VolumeTile-Android17-APKs` artifact.
5. Install either the debug APK or release APK.

If Android reports a signature mismatch, uninstall the old VolumeTile first:

```sh
adb uninstall com.jhc.volumetile
adb install app-release.apk
```
