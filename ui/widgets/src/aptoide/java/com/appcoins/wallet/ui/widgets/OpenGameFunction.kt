package com.appcoins.wallet.ui.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity

fun openGame(gamePackage: String?, context: Context) {
  try {
    val launchIntent: Intent? = gamePackage?.let {
      context.packageManager.getLaunchIntentForPackage(
        it
      )
    }
    if (launchIntent != null)
      startActivity(context, launchIntent, null)
    else
      getGame(gamePackage, context)
  } catch (e: Throwable) {
    getGame(gamePackage, context)
  }
}

fun isPackageGameInstalled(packageName: String?, packageManager: PackageManager): Boolean {
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

private fun getGame(gamePackage: String?, context: Context) {
  try {
    val intent = Intent(
      Intent.ACTION_VIEW,
      Uri.parse("market://details?id=$gamePackage")
    )
    intent.setPackage("cm.aptoide.pt")
    startActivity(context, intent, null)
  } catch (_: ActivityNotFoundException) {
    // no store, go to aptoide webpage
    val intent = Intent(
      Intent.ACTION_VIEW,
      Uri.parse("https://en.aptoide.com/")
    )
    startActivity(context, intent, null)
  }
}
