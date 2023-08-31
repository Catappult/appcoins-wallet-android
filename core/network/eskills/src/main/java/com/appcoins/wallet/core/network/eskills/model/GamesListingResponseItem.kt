package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

data class GamesListingResponseItem(

  @SerializedName("datalist") val dataList: DataList

)


data class DataList(
  @SerializedName("list") val list: List<GameInfo>

)

data class GameInfo(
  @SerializedName("name") val appName: String,
  @SerializedName("package") val packageName: String,
  @SerializedName("icon") val appIcon: String,
  @SerializedName("graphic") val background: String
)