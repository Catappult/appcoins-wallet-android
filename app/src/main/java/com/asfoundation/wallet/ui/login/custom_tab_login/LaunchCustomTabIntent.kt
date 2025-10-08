package com.asfoundation.wallet.ui.login.custom_tab_login

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/**
 * Chrome package name constant to ensure Custom Tabs open in Chrome.
 */
private const val CHROME_PACKAGE_NAME = "com.android.chrome"

/**
 * Launches a Custom Tab with the specified URL in the given context.
 *
 * This function creates a basic Custom Tabs intent, sets it to use Chrome,
 * and launches the URL.
 *
 * @param url The URL to be opened in the Custom Tab.
 * @param context The context from which to launch the Custom Tab.
 */
fun launchCustomChromeTabIntent(
  url: String,
  context: Context
) =
  CustomTabsIntent
    .Builder()
    .build()
    .launchUrl(context, "$url&is_ctt=true".toUri())