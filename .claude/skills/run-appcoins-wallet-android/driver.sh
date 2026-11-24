#!/usr/bin/env bash
# Drive the AppCoins Wallet debug build on an Android emulator over adb.
# Run from the repo root. Env: SERIAL (default emulator-5580), OUT (screenshot dir).
set -euo pipefail
SERIAL=${SERIAL:-emulator-5580}
OUT=${OUT:-/tmp/run-appcoins-wallet}
PKG=com.appcoins.wallet.dev
A() { adb -s "$SERIAL" "$@"; }
sdk() { grep '^sdk.dir' local.properties | cut -d= -f2; }
# uiautomator waits for an idle UI; animated screens never idle, so time-box it and never
# fall back to the previous dump (that returns stale screens).
dump() { A exec-out sh -c 'rm -f /sdcard/ui.xml; timeout 15 uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; cat /sdcard/ui.xml 2>/dev/null'; }
mkdir -p "$OUT"

case "${1:-help}" in
  busy) # other processes driving emulators (parallel sessions run UI sweeps on 5554/5556)
    ps -axo pid,etime,command | grep -E 'uiautomator|sweep\.py|adb -s emulator' | grep -v grep || echo "nothing else driving emulators"
    adb devices ;;
  boot) # boot [avd] [port]: read-only instance, nothing persists to the AVD
    AVD=${2:-Medium_Phone_API_36.1}; PORT=${3:-5580}
    nohup "$(sdk)/emulator/emulator" -avd "$AVD" -port "$PORT" -read-only -no-snapshot-save \
      -no-boot-anim -no-audio > "$OUT/emulator-$PORT.log" 2>&1 &
    adb -s "emulator-$PORT" wait-for-device
    adb -s "emulator-$PORT" shell 'while [ -z "$(getprop sys.boot_completed)" ]; do sleep 2; done'
    echo "booted emulator-$PORT (API $(adb -s "emulator-$PORT" shell getprop ro.build.version.sdk))" ;;
  build) ./gradlew :app:assembleGpDebug -q ;;
  install) # install [apk]: defaults to the newest gp debug APK
    APK=${2:-$(ls -t app/build/outputs/apk/gp/debug/*.apk | head -1)}
    echo "$APK"; A install -r "$APK" | tail -1 ;;
  launch) A shell am start -W -n "$PKG/com.asfoundation.wallet.main.MainActivity" | grep -E 'Status|Error' ;;
  stop) A shell am force-stop "$PKG" ;;
  game) # Trivial Drive DEV sample (com.appcoins.trivialdrivesample.test), the SDK purchase client
    A shell monkey -p com.appcoins.trivialdrivesample.test -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
    echo started ;;
  shot) A exec-out screencap -p > "$OUT/$2.png"; echo "$OUT/$2.png" ;;
  tap) A shell input tap "$2" "$3" ;;
  type) A shell input text "$2" ;;
  nodes) # visible nodes with text or content-desc, and their bounds
    dump | python3 -c '
import sys,re
for n in re.findall(r"<node [^>]*>", sys.stdin.read()):
    t=re.search(r" text=\"([^\"]*)\"",n).group(1) or re.search(r"content-desc=\"([^\"]*)\"",n).group(1)
    if t: print(repr(t)[:70], re.search(r"bounds=\"([^\"]*)\"",n).group(1))' ;;
  text) # text <regex>: tap the centre of the first node whose text/content-desc matches
    XY=$(dump | python3 -c '
import sys,re
p=re.compile(sys.argv[1])
for n in re.findall(r"<node [^>]*>", sys.stdin.read()):
    if p.search(re.search(r" text=\"([^\"]*)\"",n).group(1)) or p.search(re.search(r"content-desc=\"([^\"]*)\"",n).group(1)):
        a,b,c,d=map(int,re.findall(r"\d+",re.search(r"bounds=\"([^\"]*)\"",n).group(1))); print((a+c)//2,(b+d)//2); break' "$2")
    [ -n "$XY" ] || { echo "NOT FOUND: $2" >&2; exit 1; }
    A shell input tap $XY; echo "tapped $XY" ;;
  top) A shell dumpsys activity activities | grep -m1 topResumedActivity | grep -oE '[a-z0-9.]+/[A-Za-z0-9_.]+' ;;
  wait-top) # wait-top <regex> [seconds]: poll until the resumed activity matches
    for _ in $(seq 1 "${3:-60}"); do
      T=$("$0" top || true); if echo "$T" | grep -qE "$2"; then echo "$T"; exit 0; fi; sleep 1
    done; echo "TIMEOUT, top is: $T" >&2; exit 1 ;;
  http) # last N wallet HTTP calls (debug builds only)
    A logcat -d | grep HTTP_TRACE | grep -E 'Request encoded url|Response code|Failed request' \
      | sed -E 's/^.*HTTP_TRACE: //' | cut -c1-150 | tail -"${2:-20}" ;;
  crashes) A logcat -d | grep -E "FATAL EXCEPTION|am_crash.*$PKG|ANR in $PKG" || echo "no wallet crashes in logcat" ;;
  logclear) A logcat -c ;;
  *) sed -n '2,3p' "$0"; grep -oE '^  [a-z-]+\)' "$0" | tr -d ' )' | paste -sd' ' - ;;
esac
