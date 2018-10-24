package com.asfoundation.wallet.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreditsBalanceResponse(@JsonProperty("balance") val balance: BigDecimal)