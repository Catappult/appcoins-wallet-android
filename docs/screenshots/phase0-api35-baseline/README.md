# Phase 0 — Baseline screenshots (targetSdk 35)

Captured to compare against the post-bump build (targetSdk 36) on the **same device**,
so any difference isolates the Android 16 edge-to-edge enforcement rather than an OS delta.

- **Device**: Android 16 emulator (API 36), 1080x2400, **gesture navigation** (worst case for bottom insets)
- **Build**: `com.appcoins.wallet.dev` v4.21.0.dev, `targetSdk = 35` (opt-out flag `windowOptOutEdgeToEdgeEnforcement` still honored because targetSdk < 36)
- **Account**: logged-in dev account (Aptoide Balance)

## How to reproduce for the A/B
After bumping `Config.kt` to `targetSdk = 36` / `compileSdkVersion = 36`, rebuild + reinstall the
dev app on this same emulator and re-capture the same flows into a sibling folder
`phase1-api36/`. Diff each pair.

## Captures & what to watch

| File | Screen | Edge-to-edge risk to verify after bump |
|---|---|---|
| `01-launch.png` | Splash | `SplashTheme` uses `windowTranslucentStatus` + `fitsSystemWindows` — verify logo not clipped |
| `02-after-load.png` | Home | Top bar (Wallet/gear/help) below status bar; bottom nav clears gesture pill |
| `03-topup.png` | Top Up (method list) | **"Buy" CTA** at bottom edge — top candidate for gesture-bar overlap |
| `04-payment-card.png` | Payment success | **"OK" CTA** at bottom edge |
| `06-settings.png` | Settings (scroll list) | First item under status bar / last item under gesture bar |
| `07-rewards.png` | Rewards tab | Bottom nav overlap |
| `08-promocode.png` | Promo Code **bottom sheet** | **"Submit" CTA** — bottom sheets anchor to the bottom edge, high overlap risk |
| `09-more-sheet.png` | "More" **bottom sheet** menu | Last menu item near gesture pill |

## Expected regression after bump (before fixes)
With `targetSdk = 36` the opt-out is ignored, so all of the above draw edge-to-edge:
content slides under the status bar (top) and under the gesture/nav bar (bottom).
The bottom CTAs (`Buy`, `OK`, `Submit`) and bottom-sheet content are the most likely to
be overlapped/tappable-through. `android:statusBarColor` / `windowTranslucentStatus` in the
themes stop taking effect.
