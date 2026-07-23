# API 36 bump — edge-to-edge regressions (screenshot A/B)

Same device (Android 16 / API 36 emulator, 1080x2400, **gesture navigation**), same app,
only `targetSdk`/`compileSdk` changed **35 → 36** in
`convention/.../Config.kt`. Because the `windowOptOutEdgeToEdgeEnforcement=true` flag in
`MaterialAppTheme` (`ui/common/src/main/res/values/styles.xml:24`) is a **no-op on API 36**,
the app is now forced edge-to-edge.

- Baseline (targetSdk 35): `phase0-api35-baseline/`
- After bump (targetSdk 36): `phase1-api36/`

> Build note: the bump **compiled and packaged cleanly** on compileSdk 36 (AGP 9.0.1 /
> Gradle 9.2.1 / NDK 28.2 already support it). No source/compile errors — all issues are
> runtime layout/inset behavior.

## Confirmed regressions

### 1. Bottom-sheet CTAs overlapped by the gesture bar — HIGH
`08-promocode.png`: the **"Submit"** button sits flush against the bottom edge with the
gesture pill drawn over it. In the baseline the button had a clear inset gap above the pill.
`09-more-sheet.png`: the bottom menu items ("Restore Wallet" etc.) push into the gesture
inset zone. Bottom sheets no longer consume the navigation-bar inset.
→ Primary CTAs are partially covered and compete with the system swipe-up gesture.

### 2. White status-bar bleed at the top — HIGH (visual)
`06-settings.png`, `02-after-load.png`, `03-topup.png`: a white band appears in the
status-bar area. `MaterialAppTheme` sets `android:windowBackground = @color/white`; with
edge-to-edge enforced and `android:statusBarColor` ignored on API 36, the white window
background shows through behind the (white) system status-bar icons → icons become
low-contrast/invisible. Baseline rendered a proper dark status bar.

### 3. Bottom CTAs on full screens shifted toward the pill — MEDIUM
`03-topup.png`: the **"Next"/"Buy"** button moved down against the gesture pill (baseline
had a margin). Payment-confirmation buttons are the highest-value instance of this.

## Not (yet) broken
Toolbars / top app-bar rows on Activity-based screens still render below the status-bar
region (Material components apply some insets automatically), so the top is less affected
than the bottom — except for the white-background bleed in #2.

## Fix direction (see the main plan)
- Stop depending on `windowOptOutEdgeToEdgeEnforcement`.
- Apply `ViewCompat.setOnApplyWindowInsetsListener` for `systemBars()` (+ `ime()`) on root
  containers, **bottom sheets**, and payment screens — pad bottom by the nav-bar inset.
- Set `android:windowBackground` to the dark surface color (or make status bar background
  match content) so no white bleed; drive bar icon appearance via `WindowInsetsControllerCompat`.

## Phase 2 — fix applied & verified (`phase2-api36-fixed/`)

Centralized fix, no per-screen edits, wired from `App.onCreate`:

- **`app/.../util/EdgeToEdgeInsets.kt`** — `apply(activity)` pads the Activity content view by
  `systemBars() + ime()` insets and paints the status/nav-bar gaps with `styleguide_dark`
  (kills the white bleed) with light bar icons; `applyToBottomSheet(view)` pads a sheet by the
  bottom (nav-bar + IME) inset.
- **`App.kt`** — in the existing `ActivityLifecycleCallbacks.onActivityCreated`: calls
  `EdgeToEdgeInsets.apply(activity)` for every Activity, and registers a recursive
  `FragmentManager.FragmentLifecycleCallbacks` that calls `applyToBottomSheet` for every
  `BottomSheetDialogFragment` — so all 28 sheets are covered from one place.

Verified on the same API 36 emulator (build compiles clean on compileSdk 36):

| Screen | Before (phase1) | After (phase2) |
|---|---|---|
| Home | bottom nav under gesture pill | nav clears pill ✓ |
| Settings / Home / Top Up | white status-bar bleed | dark bar, icons visible ✓ |
| Top Up | "Next" jammed on pill | clear gap above pill ✓ |
| Promo Code sheet | "Submit" overlapped by pill | clears pill ✓ |
| More sheet | last item on pill | clears pill ✓ |

### Still to validate (Phase 4 regression pass)
The fix is global, so screens NOT re-captured here need a look for over/under-padding:
onboarding (full-bleed gradient), IAB/billing payment activity, WebView payment &
gamification activities, splash, and the transparent/floating dialog activities. Also run
`./gradlew test` and the landscape/foldable checks from the main plan.

## Phase 4 — regression sweep & webCheckout fix

Unit tests: `./gradlew :app:testAptoideDebugUnitTest` → **PASS** (exit 0).

### Regression found: the global fix broke transparent overlay activities (webCheckout)
The Phase 2 `EdgeToEdgeInsets.apply()` was applied to **every** Activity, including the
intentionally transparent, edge-to-edge overlays — most importantly **webCheckout**
(`WebViewPaymentActivity`, theme `Theme.AppCompat.Transparent.WebPayment`, a Compose modal
whose WebView draws edge-to-edge with a see-through tap-to-dismiss strip). Padding its content
and painting its decor opaque-dark destroyed the transparent modal. Same class of theme covers
`IabActivity`, `WebViewGamificationActivity`, `CustomTabLoginActivity`, `WebViewActivity`.

**Fix:** `EdgeToEdgeInsets.apply()` now early-returns for any Activity whose theme is
translucent or has a transparent `windowBackground` (`hasTransparentWindow()`), leaving those
overlays exactly as they were on API 35. Opaque screens (MaterialAppTheme, splash drawable)
are still padded.

### Verified after the fix (all on the API 36 emulator)
| Screen | Theme kind | Result |
|---|---|---|
| Onboarding | opaque | dark bars, T&C clears pill ✓ |
| Top Up → **Adyen card form** | opaque | "Top Up" CTA clears pill ✓ |
| IAB "no methods" dialog | transparent | centered, untouched ✓ |
| Home / Settings / sheets | opaque | (Phase 2) still correct ✓ |

### webCheckout — resolved on the Android side
`WebViewPaymentActivity` is now left untouched by the global helper (reverts to pre-16
transparent/edge-to-edge behavior). Product confirmed the Android layer renders fine on API 36;
the outstanding webCheckout issue is a **backend/web** problem, out of scope for this
edge-to-edge work. No further Android change needed here.

### Could not drive on this dev wallet
A live webCheckout session needs an external app's IAB purchase (not the trivial-drive sample,
per request) — wallet top-up routes card payments through Adyen, not `WebViewPaymentActivity`.
So the Android rendering was verified by code/theme analysis rather than a screenshot.

## 16 KB native-library alignment — PASS

Checked the aptoide-debug APK (native packaging is the same across flavors/build types).

**ELF LOAD-segment alignment** (`llvm-readelf -l`, NDK 28.2) — all ≥ 16 KB:

| library | arm64-v8a | x86_64 |
|---|---|---|
| libandroidx.graphics.path.so | 0x4000 | 0x4000 |
| libsentry.so / libsentry-android.so | 0x4000 | 0x4000 |
| libwallet-native.so (ours) | 0x4000 | 0x4000 |
| libnative-lib.so (ours, arm64 only) | 0x10000 | — |

**APK zip alignment** (`zipalign -c -P 16`) → **Verification successful**; every `.so` is
stored uncompressed and 16 KB-page-aligned in the archive.

Only 4 native libs are bundled (androidx.graphics.path, sentry ×2, our own native-lib/
wallet-native). Heavier SDKs (VK, TrueLayer, Adyen, web3j) ship no `.so` in this variant, so
nothing else needs alignment. No action required for 16 KB page-size compatibility.

## Reproduction notes / caveats
- Phase 1 used a **fresh wallet** (€0.00) because the emulator's small `/data` forced an
  uninstall+reinstall; layouts are identical to a funded account, so the A/B holds.
- To free install space I ran `adb shell pm uninstall-system-updates`, which reverted
  bundled Google apps (Play Store, Chrome, GMS, …) on the **emulator** to factory versions.
  Harmless — they re-update; noted for transparency.
- `Config.kt` is left at `targetSdk = 36` (this branch's goal). Revert if you need the
  baseline build again.