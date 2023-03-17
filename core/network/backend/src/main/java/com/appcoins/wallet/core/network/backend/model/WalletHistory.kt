package com.appcoins.wallet.core.network.backend.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class WalletHistory {
  @JsonProperty("result")
  var result: List<Transaction>? = null

  enum class Status {
    SUCCESS, FAIL
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  class Transaction {
    @JsonProperty("app")
    var app: String? = null

    @JsonProperty("sku")
    var sku: String? = null

    @JsonProperty("TxID")
    var txID: String? = null

    @JsonProperty("amount")
    var amount: BigInteger? = null

    @JsonProperty("paid_currency_amount")
    var paidAmount: String? = null

    @JsonProperty("paid_currency")
    var paidCurrency: String? = null

    @JsonProperty("block")
    var block: BigInteger? = null

    @JsonProperty("bonus")
    var bonus: BigDecimal? = null

    @JsonProperty("icon")
    var icon: String? = null

    @JsonProperty("receiver")
    var receiver: String? = null

    @JsonProperty("sender")
    var sender: String? = null

    @JsonProperty("ts")
    var ts: Date? = null

    @JsonProperty("processed_time")
    var processedTime: Date? = null

    @JsonProperty("type")
    var type: String? = null

    @JsonProperty("subtype")
    var subType: String? = null

    @JsonProperty("method")
    var method: String? = null

    @JsonProperty("title")
    var title: String? = null

    @JsonProperty("description")
    var description: String? = null

    @JsonProperty("perk")
    var perk: String? = null

    @JsonProperty("status")
    var status: Status? = null

    @JsonProperty("operations")
    var operations: List<Operation>? = null

    @JsonProperty("linked_tx")
    var linkedTx: List<String>? = null

    @JsonProperty("reference")
    var orderReference: String? = null

    override fun toString(): String {
      return ("Result{"
          + "txID='"
          + txID
          + '\''
          + ", amount="
          + amount
          + ", block="
          + block
          + ", receiver='"
          + receiver
          + '\''
          + ", sender='"
          + sender
          + '\''
          + ", ts='"
          + ts
          + '\''
          + ", type='"
          + type
          + '\''
          + '}')
    }
  }

  class Operation {
    @JsonProperty("TxID")
    var transactionId: String? = null
    var fee: String? = null
    var receiver: String? = null
    var sender: String? = null
  }
}