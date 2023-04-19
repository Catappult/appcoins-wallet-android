package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

class AppInfo(
  @SerializedName("data") val data: AppData
)

class AppData(
  @SerializedName("uname") val uname: String
)