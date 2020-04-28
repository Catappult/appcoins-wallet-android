package com.asfoundation.wallet.logging

import com.asfoundation.wallet.logging.LogReceiver.Companion.DEFAULT_MSG
import com.asfoundation.wallet.logging.LogReceiver.Companion.DEFAULT_TAG
import com.crashlytics.android.Crashlytics

class CrashlyticsReceiver: LogReceiver {
  override fun log(tag: String?, throwable: Throwable?) {
    Crashlytics.logException(throwable)
  }

  override fun log(tag: String?, message: String?) {
  Crashlytics.log("${tag?:DEFAULT_TAG} ${message?:DEFAULT_MSG}")
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    Crashlytics.log("${tag?: DEFAULT_TAG} ${message?: DEFAULT_MSG}")
    Crashlytics.logException(throwable)
  }
}