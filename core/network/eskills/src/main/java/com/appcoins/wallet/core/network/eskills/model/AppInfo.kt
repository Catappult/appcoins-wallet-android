package com.appcoins.wallet.core.network.eskills.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AppInfo(
  @JsonProperty("data")
  var data: AppData
)

