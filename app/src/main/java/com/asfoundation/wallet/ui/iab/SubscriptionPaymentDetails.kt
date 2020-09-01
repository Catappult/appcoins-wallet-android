package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

data class SubscriptionPaymentDetails(val subscriptionPeriod: String, val trialPeriod: String?,
                                      val introAppcAmount: BigDecimal?, val introPeriod: String?,
                                      val introCycle: Int?)
