package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TicketRequest(

  @SerializedName("package_name")
  private val packageName: String,

  @SerializedName("user_id")
  private val userId: String?,

  @SerializedName("user_name")
  private val userName: String?,

  @SerializedName("wallet_address")
  private val walletAddress: String,

  @SerializedName("room_metadata")
  private val roomMetadata: Map<String, String>,

  @SerializedName("match_environment")
  private val matchEnvironment: EskillsPaymentData.MatchEnvironment?,

  @SerializedName("number_of_users")
  private val numberOfUsers: Int?,

  @SerializedName("price")
  private val price: BigDecimal?,

  @SerializedName("price_currency")
  private val priceCurrency: String?,

  @SerializedName("sku")
  private val sku: String?,

  @SerializedName("match_max_duration")
  private val timeout: Int?,

  @SerializedName("queue_id")
  private val queue_id: String?,
)
