package com.appcoins.wallet.core.network.eskills.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AppData(
  @JsonProperty("uname")
  var uname: String,

  @JsonProperty("name")
  var name: String
)