package com.appcoins.wallet.core.utils.android_common

import android.content.Intent

interface OnNewIntentActivityHandler {
  fun getCurrentIntent(): Intent
}