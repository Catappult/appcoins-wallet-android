package com.asfoundation.wallet.iab

import com.asfoundation.wallet.iab.payment_manager.PaymentManager

interface IABView {
  val paymentManager: PaymentManager
}