package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import java.math.BigDecimal

data class CarrierVerifyData(val preselected: Boolean, val domain: String, val origin: String?,
                             val transactionType: String, val transactionData: String,
                             val currency: String, val fiatAmount: BigDecimal,
                             val appcAmount: BigDecimal, val bonusAmount: BigDecimal?,
                             val skuDescription: String, val skuId: String?)