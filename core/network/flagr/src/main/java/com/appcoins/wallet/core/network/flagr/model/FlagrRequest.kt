package com.appcoins.wallet.core.network.flagr.model


data class FlagrRequest(
  val entityID: String,
  val entityType: String,
  val entityContext: EmptyContext,
  val enableDebug: Boolean,
  val flagID: Int,
  val flagKey: String,
  val flagTags: List<String>,
  val flagTagsOperator: String,
)

data class EmptyContext(
  val context: String = ""
)