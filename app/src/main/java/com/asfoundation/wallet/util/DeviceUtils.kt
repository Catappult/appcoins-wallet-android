package com.asfoundation.wallet.util

import android.os.Build

class DeviceUtils {

  val deviceManufacturer: String
    get() = Build.MANUFACTURER

  val deviceModel: String
    get() = Build.MODEL
}
