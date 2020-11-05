package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import java.math.BigDecimal

class CarrierConfirmData(val domain: String, val skuDescription: String,
                         val paymentUrl: String, val currency: String,
                         val totalFiatAmount: BigDecimal, val totalAppcAmount: BigDecimal,
                         val bonusAmount: BigDecimal, val feeFiatAmount: BigDecimal,
                         val carrierName: String, val carrierImage: String)