package com.asfoundation.wallet.util

class DeviceInfo(private val deviceManufacturer: String, private val deviceModel: String) {

  val manufacturer get() = deviceManufacturer

  val model get() = deviceModel
}
