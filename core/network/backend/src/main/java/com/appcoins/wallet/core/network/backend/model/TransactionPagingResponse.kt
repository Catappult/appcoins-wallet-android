package com.appcoins.wallet.core.network.backend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionPagingResponse(
  @JsonProperty("items") val items: List<TransactionResponse>,
  @JsonProperty("total") val total: Int?,
  @JsonProperty("next_url") val nextUrl: String?,
  @JsonProperty("next_cursor") val nextCursor: String?
)
