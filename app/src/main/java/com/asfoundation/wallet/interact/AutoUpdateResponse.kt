package com.asfoundation.wallet.interact

data class AutoUpdateResponse(val versionCode: Int, val redirectUrl: String, val minSdk: Int,
                              val updateStores: List<String>)
