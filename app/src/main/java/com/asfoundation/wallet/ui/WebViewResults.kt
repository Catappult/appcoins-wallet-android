package com.asfoundation.wallet.ui

/**
 * Results from WebView activities.
 * - SUCCESS: Operation completed successfully.
 * - FAIL: Operation failed.
 * - USER_CANCEL: Operation was cancelled by the user.
 * - RELAUNCH_WEBVIEW: Indicates that the WebView should be relaunched.
 */
enum class WebViewResults(override val code: Int) : CodeResult {
  SUCCESS(1),
  FAIL(0),
  USER_CANCEL(2),
  RELAUNCH_WEBVIEW(3);
}

/**
 * Interface for results that include a code.
 */
sealed interface CodeResult {
  val code: Int
}