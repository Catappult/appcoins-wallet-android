package com.appcoins.wallet.networkbase

import android.content.Context
import lib.android.paypal.com.magnessdk.Environment
import lib.android.paypal.com.magnessdk.MagnesResult
import lib.android.paypal.com.magnessdk.MagnesSDK
import lib.android.paypal.com.magnessdk.MagnesSettings

object MagnesUtils {
  private var magnusResult: MagnesResult? = null

  fun start(context: Context) {
    val magnesSettingsBuilder = if (BuildConfig.DEBUG)
      MagnesSettings.Builder(context).setMagnesEnvironment(Environment.SANDBOX)
    else
      MagnesSettings.Builder(context).setMagnesEnvironment(Environment.LIVE)

    MagnesSDK.getInstance().setUp(magnesSettingsBuilder.build())
  }

  fun collectAndSubmit(context: Context) {
    MagnesSDK.getInstance().collectAndSubmit(context)
  }

  fun getMetadataId(): String? {
    return magnusResult?.paypalClientMetaDataId
  }
}
