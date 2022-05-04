package com.asfoundation.wallet.logging

import com.appcoins.wallet.commons.LogReceiver
import com.asf.wallet.BuildConfig
import com.appcoins.wallet.commons.LogReceiver.Companion.DEFAULT_MSG
import com.appcoins.wallet.commons.LogReceiver.Companion.DEFAULT_THROWABLE_MSG
import com.flurry.android.FlurryAgent

class FlurryReceiver : LogReceiver {
  companion object {
    private const val DEFAULT_ERROR_ID = "ID"
  }
  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      throwable.printStackTrace()
      if (!BuildConfig.DEBUG) {
        FlurryAgent.onError(tag ?: DEFAULT_ERROR_ID, throwable.message ?: DEFAULT_THROWABLE_MSG, throwable)
      }
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
    message?.let {
      if (!BuildConfig.DEBUG) {
        FlurryAgent.onError(tag ?: DEFAULT_ERROR_ID, message, Throwable())
      }
    }
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    if (!BuildConfig.DEBUG) {
      throwable?.let { FlurryAgent.onError(tag ?: DEFAULT_ERROR_ID, message ?: DEFAULT_MSG, it) }
    }
  }
}