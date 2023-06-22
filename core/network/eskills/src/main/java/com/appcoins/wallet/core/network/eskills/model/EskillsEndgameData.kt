package com.appcoins.wallet.core.network.eskills.model

data class EskillsEndgameData(
  var scheme: String,
  var host: String,
  var path: String,
  var parameters: MutableMap<String, String?>,
  var packageName: String,
  var session: String,
)
