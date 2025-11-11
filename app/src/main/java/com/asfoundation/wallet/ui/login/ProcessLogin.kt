package com.asfoundation.wallet.ui.login

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.native_login.NativeLoginActivity
import com.asfoundation.wallet.ui.login.webview_login.WebViewLoginActivity


/**
 * The message that should be displayed in the Toast after the login.
 */
const val RESPONSE_TOAST_MESSAGE = "response_toast_message"

/**
 * Process the login request by checking if there is a browser that supports Custom Tabs.
 * If there is, launch the Custom Tab with the provided URL.
 * If not, fallback to the WebViewLoginActivity.
 *
 * @param url The URL to load for login.
 * @param context The context to use for launching activities.
 * @param launcher An optional launcher for launching activities.
 * @see CustomTabLoginActivity
 * @see WebViewLoginActivity
 */
fun processLoginRequest(
  url: String,
  context: Context,
  launcher: ActivityResultLauncher<Intent>? = null
) {
  val hasCustomChromeTabAvailable = hasCustomChromeTabAvailable(context)
  val useUrl = url
    .addIsCctParamToUrl(hasCustomChromeTabAvailable)
    .addVersionParamToUrl(BuildConfig.VERSION_CODE.toString())
  val intent =
    if (hasCustomChromeTabAvailable) {
      Intent(context, NativeLoginActivity::class.java)
        .apply { putExtra(NativeLoginActivity.URL, useUrl) }
    } else {
      Intent(context, WebViewLoginActivity::class.java)
        .apply { putExtra(WebViewLoginActivity.URL, useUrl) }
    }
  launcher
    ?.launch(intent)
    ?: run { context.startActivity(intent) }
}

/**
 * Adds the "is_cct" parameter to the URL to indicate whether Custom Tabs are being used.
 *
 * @param this@addIsCctParamToUrl The original URL.
 * @param isCct A boolean indicating if Custom Tabs are being used.
 * @return The modified URL with the "is_cct" parameter appended.
 */
private fun String.addIsCctParamToUrl(isCct: Boolean): String {
  val separator = if (contains("?")) "&" else "?"
  return "${this}${separator}is_cct=$isCct"
}

/**
 * Adds the "version" parameter to the URL to indicate the app version.
 *
 * @param this@addVersionParamToUrl The original URL.
 * @param version The app version to append.
 * @return The modified URL with the "app_version" parameter appended.
 */
private fun String.addVersionParamToUrl(version: String): String {
  val separator = if (contains("?")) "&" else "?"
  return "${this}${separator}version=$version"
}