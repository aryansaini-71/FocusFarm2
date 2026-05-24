# Focus Farm 2 / Anti-Brain Rot

Native Android (Java + XML) hackathon app that intercepts distracting apps via **AccessibilityService** and shows a cognitive friction overlay with breathing + quiz mini-games.

## Project structure

```
app/src/main/java/com/antibrainrot/app/
├── ui/           # Activities & Fragments
├── service/      # Accessibility + overlay controller
├── data/         # SharedPreferences (locks, points, plants)
└── util/         # Accessibility permission helpers
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for judges / teammates.

## Quick start

1. Open in **Android Studio** and sync Gradle.
2. Run on a **physical device**.
3. Enable **Anti-Brain Rot** under Settings → Accessibility.
4. Open Instagram or TikTok to test the overlay.

## Key classes

| Class | Role |
|-------|------|
| `ui.main.MainActivity` | Bottom nav shell |
| `service.AppInterceptorService` | Foreground app detection |
| `service.overlay.OverlayController` | WindowManager UI + mini-games |
| `service.InterceptPolicy` | When to show overlay |
| `data.InterceptPrefs` | Lock / unblock / cooldown timestamps |
| `data.GameDataManager` | Points & plants |
