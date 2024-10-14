package com.appcoins.wallet.core.utils.android_common

import android.content.Context

object RedirectUtils {
  private const val REDIRECT_RESULT_SCHEME = "appcoins://"

  fun getReturnUrl(context: Context): String {
    return REDIRECT_RESULT_SCHEME + context.packageName
  }
}