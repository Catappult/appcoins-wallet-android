package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TransactionOverviewResponse(
  @SerializedName("txid") val transactionId: String,
  val type: BackendTransactionType,
  val sender: String,
  val receiver: String,
  @SerializedName("usd_amount") val usdAmount: BigDecimal,
  val timestamp: String,
  @SerializedName("payment_method") val paymentMethod: String,
  @SerializedName("package_name") val packageName: String,
  val country: String,
)

enum class BackendTransactionType {
  BONUS_REVERT, TOPUP_REVERT, WALLET_TOPUP, BONUS_REFERRAL, IAP_REVERT, SUBSCRIPTION_REVERT,
  BONUS_REFUND, TOPUP_REFUND, IAP_REFUND, SUBSCRIPTION_REFUND, POA_REJECTED, ICO, BONUS_GIFTCARD,
  IAP, VOUCHER, ESKILLS, ESKILLS_REVERT, ESKILLS_REFUND, ESKILLS_WITHDRAW, ESKILLS_REWARD, TRANSFER,
  BONUS, WEB_TOPUP, POA, CAMPAIGN_CANCELLATION, APPROVAL, SUBSCRIPTION, CAMPAIGN_CREATION, WITHDRAW,
  UNKNOWN, FEE, BURN
}
