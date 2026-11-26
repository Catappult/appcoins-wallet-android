#!/usr/bin/env bash
# Checks that a release APK is the one we mean to publish. Exits non-zero on the first problem.
# Usage: verify-release-apk.sh <apk> <versionName> <versionCode>
set -euo pipefail

APK=$1
EXPECTED_NAME=$2
EXPECTED_CODE=$3
EXPECTED_PACKAGE=com.appcoins.wallet
# SHA-256 of the production signing certificate (CN=Blockchain Distribution Services).
EXPECTED_CERT=b46d2e4a263b41c86f6d1fe490b72796ded4c27f10a5de1e22de1d14985c1a74

SDK=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}
BUILD_TOOLS=$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)

fail() { echo "FAIL $(basename "$APK"): $1" >&2; exit 1; }

badging=$("$BUILD_TOOLS/aapt2" dump badging "$APK")
grep -q "^package: name='$EXPECTED_PACKAGE' versionCode='$EXPECTED_CODE' versionName='$EXPECTED_NAME' " <<<"$badging" \
  || fail "expected $EXPECTED_PACKAGE $EXPECTED_NAME ($EXPECTED_CODE), got: $(grep '^package:' <<<"$badging")"
grep -q "application-debuggable" <<<"$badging" && fail "APK is debuggable"

cert=$("$BUILD_TOOLS/apksigner" verify --print-certs "$APK" | sed -n 's/^Signer #1 certificate SHA-256 digest: //p')
[ "$cert" = "$EXPECTED_CERT" ] || fail "signed with certificate ${cert:-<none>}, expected the production one"

# Without this UUID Sentry cannot match crashes to the uploaded R8 mapping.
uuid=$(unzip -p "$APK" assets/sentry-debug-meta.properties 2>/dev/null | sed -n 's/^io\.sentry\.ProguardUuids=//p' || true)
[[ "$uuid" =~ ^[0-9a-f-]{36}$ ]] || fail "no Sentry mapping UUID in the APK"

echo "OK $(basename "$APK"): $EXPECTED_NAME ($EXPECTED_CODE), production certificate, Sentry UUID $uuid"
