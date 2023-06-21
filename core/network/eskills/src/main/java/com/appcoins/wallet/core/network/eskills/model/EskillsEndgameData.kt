package com.appcoins.wallet.core.network.eskills.model

data class EskillsEndgameData(
  var scheme: String,
  var host: String,
  var path: String,
  var parameters: MutableMap<String, String?>,
  var walletAddress: String,
  var packageName: String,
  var product: String,
  var session: String,
  var userScore: Long,
)
