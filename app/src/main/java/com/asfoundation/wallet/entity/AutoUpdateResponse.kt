package com.asfoundation.wallet.entity

import com.google.gson.annotations.SerializedName

data class AutoUpdateResponse(@SerializedName("soft_update")
                              val softUpdate: SoftUpdateResponse,
                              @SerializedName("black_list")
                              val blackList: List<Int>)

data class SoftUpdateResponse(@SerializedName("version_code") val versionCode: Int,
                              val minSdk: Int)
