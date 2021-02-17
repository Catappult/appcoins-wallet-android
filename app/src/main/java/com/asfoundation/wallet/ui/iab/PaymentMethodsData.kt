package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

//FiatValue only available when type == Voucher
data class PaymentMethodsData(val appPackage: String, val isBds: Boolean,
                              val developerPayload: String?, val uri: String?,
                              val transactionValue: BigDecimal, val fiatValue: FiatValue?)