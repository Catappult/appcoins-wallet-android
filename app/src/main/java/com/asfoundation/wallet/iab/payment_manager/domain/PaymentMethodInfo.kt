package com.asfoundation.wallet.iab.payment_manager.domain

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity

data class PaymentMethodInfo(
  val paymentMethod: PaymentMethodEntity,
  val balance: String?
)
