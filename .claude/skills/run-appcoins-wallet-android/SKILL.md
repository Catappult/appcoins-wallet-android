---
name: run-appcoins-wallet-android
description: Build, install, launch and drive the AppCoins Wallet Android debug app on an emulator. Use when asked to run or start the wallet, smoke-test it, take screenshots, do a top-up or an in-app purchase (Trivial Drive sample), check crashes or HTTP calls, or run its unit tests.
---

Debug build (`gpDebug`, package `com.appcoins.wallet.dev`) driven over adb by
`.claude/skills/run-appcoins-wallet-android/driver.sh`. All paths are relative to the repo root.
Verified on a macOS host with the Android SDK from `local.properties`.

## Prerequisites

- Android SDK with emulator and the `Medium_Phone_API_36.1` AVD (API 36, Play Store image).
- Untracked files that are not in git. Copy them from an existing checkout or a teammate:
  `local.properties`, `app/google-services.json`, `app/src/debug/google-services.json`,
  `app/src/release/google-services.json`, `app/libs/android-magnessdk-5.3.0.aar`,
  `core/network/base/libs/android-magnessdk-5.3.0.aar`, `libs/vk-maven/`.
- For purchases: the Trivial Drive DEV sample APK (`com.appcoins.trivialdrivesample.test` 1.5.10 DEV,
  not in the repo). It talks to `com.appcoins.wallet.dev` and the dev backend.

## Run (agent path)

```bash
D=.claude/skills/run-appcoins-wallet-android/driver.sh   # SERIAL defaults to emulator-5580
$D busy        # is another session driving an emulator? never share one
$D boot        # Medium_Phone_API_36.1 on emulator-5580, -read-only (resets at shutdown)
$D build       # ./gradlew :app:assembleGpDebug, ~1 min incremental
$D install     # newest app/build/outputs/apk/gp/debug/*.apk
$D install "$TRIVIAL_APK"
$D launch      # "Status: timeout" + ANR dialog on a fresh boot is normal, see Gotchas
$D text '^Wait$'
$D text 'Don.t allow'
$D shot home   # -> /tmp/run-appcoins-wallet/home.png; Read it to look
```

Top-up (€10, saved Adyen test card):

```bash
$D text '^Top Up$'
$D wait-top TopUpActivity 30
$D text '^Buy$'                                   # ~10 s after the screen opens
$D nodes | grep -E 'Success|deposited'            # ~30 s later
$D text '^OK$'
```

In-app purchase (Gas in Trivial Drive, paid from the balance, through `WebViewPaymentActivity`):

```bash
$D game; $D wait-top trivialdrivesample 30
$D text '^OK$'                                    # 16 KB compatibility dialog of the sample
$D logclear; $D tap 723 1155                      # Buy Gas (no label; 1080x2400 screen)
$D wait-top WebViewPaymentActivity 90
for i in 1 2 3 4 5 6 7 8; do sleep 10; $D nodes | grep -q 'Confirm purchase' && break; done
$D text 'Confirm purchase'
$D wait-top trivialdrivesample 90
$D nodes | grep -iE 'filled|tank'                 # "You filled 1/4 tank..."
$D crashes
adb -s emulator-5580 emu kill                     # done
```

| command | what it does |
|---|---|
| `busy` | lists host processes driving emulators, plus `adb devices` |
| `boot [avd] [port]` | starts a read-only emulator, waits for `sys.boot_completed` |
| `build` / `install [apk]` / `launch` / `stop` / `game` | build, install, start wallet, force-stop, start Trivial Drive |
| `shot <name>` | screenshot to `$OUT/<name>.png` (default `/tmp/run-appcoins-wallet`) |
| `tap x y` / `type <text>` | raw input in device pixels |
| `nodes` / `text <regex>` | list labelled nodes / tap the first node whose text or content-desc matches |
| `top` / `wait-top <regex> [s]` | resumed activity / poll until it matches |
| `http [n]` | last wallet HTTP calls and response codes (debug builds log `HTTP_TRACE`) |
| `crashes` / `logclear` | wallet FATAL/ANR lines in logcat / clear logcat |

## Test

```bash
./gradlew :core:network:base:testGpDebugUnitTest
./gradlew :app:testGpDebugUnitTest        # 198 tests pass, several minutes
```

## Gotchas

- **Other Claude sessions run UI sweeps (`sweep.py`) on emulator-5554/5556.** Sharing one makes both
  sessions' taps land in each other's flows. Run `busy` first and use your own port (5580).
- **This AVD already has a dev wallet in its snapshot** (5.0.3.dev, with a balance and a saved test
  Visa ending 1111), plus the production wallet `com.appcoins.wallet`. `install -r` keeps that data,
  which is why top-up offers the saved card. Local changes vanish at shutdown (`-read-only`); the
  balance lives on the dev backend and persists.
- **No real money.** Debug builds use `api.dev.catappult.io` / `apichain.dev.catappult.io` and Adyen
  `Environment.TEST` (`AppModule.provideAdyenEnvironment`).
- **First launch after boot ANRs**: "Aptoide Wallet isn't responding", reason "failed to complete
  startup". The emulator's load average is 15–35 even when idle, so waiting for it to calm down never
  ends. Tap Wait and the app loads.
- **`uiautomator dump` waits for an idle UI**, and animated screens (wallet home countdowns) rarely
  idle. The driver time-boxes it to 15 s and returns nothing rather than the previous dump; a stale
  dump once showed a dismissed permission dialog for minutes. Label taps take ~12 s on home. On
  `NOT FOUND`, take a screenshot and tap coordinates (screenshots are 1080x2400 device pixels).
- **WebView content is in the accessibility tree**, so `text 'Confirm purchase'` works. Prefer it to
  coordinates: the checkout layout changes with offers (first-purchase-free banner vs cashback banner
  moved the button by ~200 px between two runs).
- **The web checkout needs ~35–60 s** after `WebViewPaymentActivity` opens: black, then white, then a
  spinner, then the page. `TopUpActivity` is black for ~10 s on first open.
- **Sweeps leave display overrides** (`Override size: 1600x2560` seen on emulator-5556). Check
  `adb shell wm size` before reusing coordinates.

## Troubleshooting

- **`Unresolved reference 'MagnesSDK'`** in `:core:network:base`: the untracked
  `core/network/base/libs/android-magnessdk-5.3.0.aar` is missing. Copy it in.
- **`INSTALL_FAILED_INSUFFICIENT_STORAGE`**: the AVD's 6 GB `/data` is 97% full.
  `adb -s emulator-5580 shell pm trim-caches 4G`, then install again.
- **`task 'testGpReleaseUnitTest' not found`** in a library module: libraries only have
  `testGpDebugUnitTest` / `testAptoideDebugUnitTest`.
- **JaCoCo `IllegalClassFormatException` stack traces during tests**: instrumentation noise; read the
  XML results in `build/test-results/` instead.
