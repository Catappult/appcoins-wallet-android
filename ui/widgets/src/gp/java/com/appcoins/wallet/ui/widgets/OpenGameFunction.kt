package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.startActivity


fun openGame(
  gamePackage: String?,
  actionUrl: String?,
  context: Context,
  sendPromotionClickEvent: (String?, String) -> Unit
) {
  try {
    val launchIntent: Intent? = gamePackage?.let {
      context.packageManager.getLaunchIntentForPackage(
        it
      )
    }
    if (launchIntent != null) {
      sendPromotionClickEvent(gamePackage, "open")
      startActivity(context, launchIntent, null)
    }
  } catch (e: Throwable) {
  }
}

fun isPackageInstalled(packageName: String?, packageManager: PackageManager): Boolean {
  if (packageName == null) {
    return false
  }
  return try {
    packageManager.getPackageInfo(packageName, 0)
    true
  } catch (e: PackageManager.NameNotFoundException) {
    false
  }
}

