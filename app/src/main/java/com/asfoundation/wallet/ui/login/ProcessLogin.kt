package com.asfoundation.wallet.ui.login

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import androidx.browser.customtabs.CustomTabsClient
import androidx.core.net.toUri
import com.asfoundation.wallet.ui.login.custom_tab_login.launchCustomChromeTabIntent
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity
import com.asfoundation.wallet.ui.login.webview_login.WebViewLoginActivity


/**
 * Dummy domain to test if there is any app that can handle the custom tab intent.
 */
private const val DUMMY_DOMAIN = "http://www.example.com"
private val activityIntent = Intent(ACTION_VIEW, DUMMY_DOMAIN.toUri())

/**
 * Process the login request by checking if there is a browser that supports Custom Tabs.
 * If there is, launch the Custom Tab with the provided URL.
 * If not, fallback to the WebViewLoginActivity.
 *
 * @param url The URL to load for login.
 * @param context The context to use for launching activities.
 * @see CustomTabLoginActivity
 * @see WebViewLoginActivity
 */
fun processLoginRequest(
  url: String,
  context: Context
) {
  val packageNames =
    context
      .packageManager
      .queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
      .map { it.activityInfo.packageName }

  CustomTabsClient.getPackageName(
    context,
    packageNames,
    true
  )?.let {
    launchCustomChromeTabIntent(
      url = url,
      context = context
    )
  } ?: run {
    Intent(context, WebViewLoginActivity::class.java)
      .apply { putExtra(WebViewLoginActivity.URL, "$url&is_ctt=false") }
      .also { context.startActivity(it) }
  }
}