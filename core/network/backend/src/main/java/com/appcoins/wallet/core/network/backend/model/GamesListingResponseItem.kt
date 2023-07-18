package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class GamesListingResponseItem(

/*
  @SerializedName("package_name") val packageName: String,
  @SerializedName("app_name") val appName: String,
  @SerializedName("app_icon") val appIcon: String,
  @SerializedName("background") val background: String,
   */

  @SerializedName("datalist") val dataList: DataList
  )

data class DataList (
  @SerializedName("list") val list: List<GameInfo>

  )

data class GameInfo (
  @SerializedName("name") val appName: String,
  @SerializedName("package") val packageName: String,
  @SerializedName("icon") val appIcon: String,
  @SerializedName("graphic") val background: String
        )
