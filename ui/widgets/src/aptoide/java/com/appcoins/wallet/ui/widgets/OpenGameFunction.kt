package com.appcoins.wallet.ui.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity

fun openGame(gamePackage: String, context: Context) {
  try {
    val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(gamePackage)
    if (launchIntent != null)
      startActivity(context, launchIntent, null)
    else
      getGame(gamePackage, context)
  } catch (e: Throwable) {
    getGame(gamePackage, context)
  }
}

private fun getGame(gamePackage: String, context: Context) {
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
