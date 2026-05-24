# Anti-Brain Rot (Hackathon Starter)

Native Android (Java + XML) app that intercepts Instagram and TikTok via an **AccessibilityService** and shows a 60-second cognitive friction overlay.

## Quick start

1. Open this folder in **Android Studio** (Ladybug or newer recommended).
2. Let Gradle sync, then run on a **physical device** (accessibility overlays are unreliable on some emulators).
3. On the device: open the app → **Open Accessibility Settings** → enable **Anti-Brain Rot**.
4. Open Instagram (`com.instagram.android`) or TikTok (`com.zhiliaoapp.musically`) to see the overlay.

## Key files

| File | Purpose |
|------|---------|
| `AppInterceptorService.java` | Detects foreground app, shows overlay, 60s timer |
| `interceptor_overlay.xml` | Full-screen UI layout |
| `accessibility_service_config.xml` | Service capabilities + package filter |
| `AndroidManifest.xml` | Service registration |

## TODO hooks for your team

- **Database / lock state** — `onAccessibilityEvent()` and `onDismissRequested()`
- **Mini-game** — inflate your View into `R.id.minigame_container`
- **Pass Test** — `onPassTestRequested()` after validating the game

## Notes

- TikTok’s package name can vary by region (`com.ss.android.ugc.trill`). Add extras to `BLOCKED_PACKAGES` and `packageNames` in the XML config if needed.
- Users must manually grant Accessibility permission; this is required by Android for this approach.
