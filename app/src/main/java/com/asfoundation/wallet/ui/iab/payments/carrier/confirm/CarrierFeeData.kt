package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import java.math.BigDecimal

data class CarrierFeeData(val uid: String, val domain: String, val transactionData: String,
                          val transactionType: String, val skuDescription: String,
                          val skuId: String?, val paymentUrl: String, val currency: String,
                          val fiatAmount: BigDecimal, val appcAmount: BigDecimal,
                          val bonusAmount: BigDecimal?, val feeFiatAmount: BigDecimal,
                          val carrierName: String, val carrierImage: String,
                          val phoneNumber: String)