# Architecture

## Layers

| Package | Responsibility |
|---------|----------------|
| `ui.*` | XML layouts, Fragments, MainActivity navigation |
| `service` | `AppInterceptorService`, `InterceptPolicy` |
| `service.overlay` | `OverlayController` — WindowManager, timers, dialogs |
| `data` | `InterceptPrefs`, `GameDataManager`, `BlockedAppsPrefs` |
| `ui.blocker` | `BlockerFragment`, `AppListAdapter`, dynamic app picker |
| `util` | `AccessibilityUtils` |

## Interceptor flow

1. `AppInterceptorService` receives `TYPE_WINDOW_STATE_CHANGED`.
2. `InterceptPolicy.shouldIntercept()` checks `BLOCKED_APPS` SharedPreferences, unblock, cooldown.
3. `OverlayController.show()` attaches `interceptor_overlay.xml`.
4. User completes breathing/quiz or locks the app.
5. `GameDataManager` updates points/plants; `InterceptPrefs` stores timers.

## Stability notes

- `CountDownTimer` cancelled in `OverlayController.hide()`.
- `Handler.removeCallbacksAndMessages(null)` clears pending UI work on teardown.
- Dismiss sends `GLOBAL_ACTION_HOME` before lock dialog to avoid overlay bounce-back.
