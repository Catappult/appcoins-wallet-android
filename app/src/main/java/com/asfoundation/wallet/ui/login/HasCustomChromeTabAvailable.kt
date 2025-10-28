package com.asfoundation.wallet.ui.login

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import androidx.browser.customtabs.CustomTabsClient
import androidx.core.net.toUri

/**
 * Dummy domain to test if there is any app that can handle the custom tab intent.
 */
private const val DUMMY_DOMAIN = "http://www.example.com"

/**
 * Intent to check for Custom Tab availability.
 */
private val activityIntent = Intent(ACTION_VIEW, DUMMY_DOMAIN.toUri())

/**
 * Checks if there is a browser available that supports Custom Tabs.
 *
 * @param context The context to use for checking the package manager.
 * @return True if a Custom Tab supporting browser is available, false otherwise.
 */
fun hasCustomChromeTabAvailable(
  context: Context
): Boolean =
  context
    .packageManager
    .queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
    .map { it.activityInfo.packageName }
    .let { packagesName ->
      CustomTabsClient
        .getPackageName(
          /* context = */ context,
          /* packages = */ packagesName,
          /* ignoreDefault = */ true
        ) != null
    }