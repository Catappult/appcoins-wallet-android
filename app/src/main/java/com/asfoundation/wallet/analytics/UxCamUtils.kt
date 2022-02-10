package com.asfoundation.wallet.analytics

import android.content.Context
import com.asf.wallet.BuildConfig
import com.uxcam.UXCam

class UxCamUtils (private val context: Context) {
  val countriesFilterList = listOf( "RU",
                                    "US",
                                    "PH",
                                    "IN",
                                    "ID",
                                    )

  fun initialize() {
    val locale: String = context.resources.configuration.locale.getCountry()
    if (countriesFilterList.contains(locale)) {
      UXCam.startWithKey(BuildConfig.UXCAM_API_KEY)
    }
  }
}