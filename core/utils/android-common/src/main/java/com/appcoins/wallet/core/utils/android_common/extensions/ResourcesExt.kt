package com.appcoins.wallet.core.utils.android_common.extensions

import android.content.Context
import androidx.annotation.DrawableRes

fun Context.getDrawableURI(@DrawableRes resId: Int): String {
//  android.resource://com.android.camera2/123456
  return "android.resource://${this.packageName}/$resId"
}