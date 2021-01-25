package com.asfoundation.wallet.ui.iab.payments.carrier.status

import java.math.BigDecimal

data class CarrierPaymentData(val domain: String, val transactionData: String,
                              val transactionType: String, val skuId: String?,
                              val paymentUrl: String, val currency: String,
                              val appcAmount: BigDecimal, val bonusAmount: BigDecimal?,
                              val phoneNumber: String)