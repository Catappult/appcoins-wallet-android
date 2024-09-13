package com.appcoins.wallet.ui.common

import android.content.Context
import android.graphics.drawable.Drawable

fun Context.getAppIconDrawable(packageName: String): Drawable? =
  packageManager.getPackageInfo(packageName)?.applicationInfo?.loadIconDrawable(packageManager)

fun Context.getAppName(packageName: String): String =
  packageManager.getPackageInfo(packageName)
    ?.applicationInfo
    ?.loadLabel(packageManager)
    ?.toString()
    ?: ""
