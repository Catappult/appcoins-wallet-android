package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class AutoUpdateResponse(@SerializedName("latest_version")
                              val latestVersion: LatestVersionResponse,
                              @SerializedName("black_list")
                              val blackList: List<Int>)

data class LatestVersionResponse(@SerializedName("version_code") val versionCode: Int,
                                 @SerializedName("min_sdk") val minSdk: Int)
