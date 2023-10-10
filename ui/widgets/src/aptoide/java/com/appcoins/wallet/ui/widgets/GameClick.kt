package com.appcoins.wallet.ui.widgets

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat

class GameClick(gamePackage: String?, context: Context) : HandleGameClick {

  init {
    try {
      val launchIntent: Intent? = gamePackage?.let {
        context.packageManager.getLaunchIntentForPackage(it)
      }
      if (launchIntent != null)
        ContextCompat.startActivity(context, launchIntent, null)
      else
        getGame(gamePackage, context)
    } catch (e: Throwable) {
      getGame(gamePackage, context)
    }
  }

  private fun getGame(gamePackage: String?, context: Context) {
    try {
      val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$gamePackage")
      )
      intent.setPackage("cm.aptoide.pt")
      ContextCompat.startActivity(context, intent, null)
    } catch (_: ActivityNotFoundException) {
      // no store, go to aptoide webpage
      val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://en.aptoide.com/")
      )
      ContextCompat.startActivity(context, intent, null)
    }
  }
}