package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class PayFlowResponse(
  @SerializedName("payment_methods")
  val paymentMethods: PaymentMethods?
)

data class PaymentMethods(
  @SerializedName("wallet_web_view_payment")
  val walletWebViewPayment: WalletWebViewPayment?,
  @SerializedName("wallet_app")
  val walletApp: WalletApp?
)

data class WalletApp(
  val priority: Int?
)

data class WalletWebViewPayment(
  val priority: Int,
  @SerializedName("screen_details")
  val screenDetails: ScreenDetails
)

data class ScreenDetails(
  @SerializedName("force_screen_orientation")
  val forceScreenOrientation: Int,
  val landscape: Landscape,
  val portrait: Portrait
)

data class Landscape(
  val width_percentage: Double,
  val height_percentage: Double
)

data class Portrait(
  val height_dp: Int,
  val width_percentage: Double,
  val height_percentage: Double
)

