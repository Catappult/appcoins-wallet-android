package com.asfoundation.wallet.entity

import com.google.gson.annotations.SerializedName

data class AutoUpdateResponse(@SerializedName("latest_version")
                              val latestVersion: LatestVersionResponse,
                              @SerializedName("black_list")
                              val blackList: List<Int>)

data class LatestVersionResponse(val versionCode: Int, val minSdk: Int)
