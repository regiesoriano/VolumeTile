# VolumeTile Android 17 fast dialog experiment

This build tries a faster no-foreground-service path:

- Tile click calls TileService.showDialog() with a 1x1 transparent dialog.
- showDialog() collapses Quick Settings without launching an Activity.
- While the dialog is shown, the app calls AudioManager.adjustStreamVolume(... ADJUST_SAME, FLAG_SHOW_UI).
- The dialog dismisses after 180 ms.

This may be faster and avoid the foreground-service notification path. It is experimental: if Android 17 audio hardening does not consider the TileService dialog a valid visible state, the volume panel may not appear. If that happens, use the fast FGS v3 build instead.
