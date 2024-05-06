package com.appcoins.wallet.core.network.backend.model

data class IntercomAttributesRequest(
  val tags: List<String>,
  val attributes: Map<String, Any>
)
