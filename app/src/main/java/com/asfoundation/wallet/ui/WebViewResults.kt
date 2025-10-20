package com.asfoundation.wallet.ui

/**
 * Results from WebView activities.
 * - SUCCESS: Operation completed successfully.
 * - FAIL: Operation failed.
 * - USER_CANCEL: Operation was cancelled by the user.
 * - RELAUNCH_WEBVIEW: Indicates that the WebView should be relaunched.
 */
enum class WebViewResults : CodeResult {
  SUCCESS {
    override val code = 1
  },
  FAIL {
    override val code = 0
  },
  USER_CANCEL {
    override val code = 2
  },
  RELAUNCH_WEBVIEW {
    override val code = 3
  }
}

/**
 * Interface for results that include a code.
 */
sealed interface CodeResult {
  val code: Int
}