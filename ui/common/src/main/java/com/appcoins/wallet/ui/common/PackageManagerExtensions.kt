package com.appcoins.wallet.ui.common

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.annotation.RequiresApi

private const val oldFlags = PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES

@get:RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val newFlags: PackageManager.PackageInfoFlags
  get() = PackageManager.PackageInfoFlags.of(oldFlags.toLong())

fun PackageManager.getPackageInfo(packageName: String): PackageInfo? = try {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    getPackageInfo(packageName, newFlags)
  } else {
    getPackageInfo(packageName, oldFlags)
  }
} catch (e: Throwable) {
  null
}

fun ApplicationInfo.loadIconDrawable(packageManager: PackageManager): Drawable =
  loadUnbadgedIcon(packageManager)
    .let { drawable ->
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
        InsetDrawable(
          LayerDrawable(listOf(drawable.background, drawable.foreground).toTypedArray()),
          -27f / 108f
        )
      } else {
        drawable
      }
    }
