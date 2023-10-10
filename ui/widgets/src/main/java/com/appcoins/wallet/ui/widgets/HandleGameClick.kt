package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.startActivity

interface HandleGameClick {
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
}

