package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

data class PaymentMethodsData(val appPackage: String, val isBds: Boolean,
                              val developerPayload: String?,
                              val uri: String?, val transactionValue: BigDecimal,
                              val frequency: String?, val subscription: Boolean)