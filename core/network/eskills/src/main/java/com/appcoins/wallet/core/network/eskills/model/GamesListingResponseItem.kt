package com.appcoins.wallet.core.network.eskills.model

import com.fasterxml.jackson.annotation.JsonProperty
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
  @SerializedName("graphic") val background: String,

  @SerializedName("file")
  val file: File
)

data class File(
  @SerializedName("md5sum")
  val md5: String
)