package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import java.math.BigDecimal

data class CarrierConfirmData(val uid: String, val domain: String, val transactionData: String,
                              val transactionType: String, val skuDescription: String,
                              val paymentUrl: String, val currency: String,
                              val totalFiatAmount: BigDecimal, val totalAppcAmount: BigDecimal,
                              val bonusAmount: BigDecimal, val feeFiatAmount: BigDecimal,
                              val carrierName: String, val carrierImage: String)