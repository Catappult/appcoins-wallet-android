package com.asfoundation.wallet.ui

import java.math.BigDecimal


data class PaymentNavigationData(val gamificationLevel: Int,
                                 val selectedPaymentId: String,
                                 val selectedPaymentIcon: String?,
                                 val selectedPaymentLabel: String?,
                                 val fiatAmount: BigDecimal,
                                 val fiatCurrency: String,
                                 val bonusMessageValue: String)
