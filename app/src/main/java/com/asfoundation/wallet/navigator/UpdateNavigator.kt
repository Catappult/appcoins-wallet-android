package com.asfoundation.wallet.navigator

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

class UpdateNavigator {

  fun navigateToStoreAppView(context: Context?, url: String) {
    context?.let {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      val packageManager = context.packageManager
      val appsList =
          packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
      appsList?.let {
        for (info in appsList) {
          if (info.activityInfo.packageName == "cm.aptoide.pt") {
            intent.setPackage(info.activityInfo.packageName)
            break
          }
          if (info.activityInfo.packageName == "com.android.vending")
            intent.setPackage(info.activityInfo.packageName)
        }
      }
      context.startActivity(intent)
    }
  }
}