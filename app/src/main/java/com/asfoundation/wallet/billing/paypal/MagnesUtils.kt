package com.asfoundation.wallet.billing.paypal

import android.content.Context
import lib.android.paypal.com.magnessdk.MagnesResult
import lib.android.paypal.com.magnessdk.MagnesSDK
import lib.android.paypal.com.magnessdk.MagnesSettings

class MagnesUtils {
  companion object {

    var magnusResult: MagnesResult? = null

    fun start(context: Context) {
      val magnesSettings = MagnesSettings.Builder(context).build()
      MagnesSDK.getInstance().setUp(magnesSettings)
    }

    fun collectAndSubmit(context: Context): MagnesResult? {
      magnusResult = MagnesSDK.getInstance().collectAndSubmit(context)
      return magnusResult
    }

    fun getMetadataId(): String? {
      return magnusResult?.paypalClientMetaDataId
    }

  }
}
