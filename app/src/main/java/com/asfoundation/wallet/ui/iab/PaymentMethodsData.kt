package com.asfoundation.wallet.ui.iab

import java.math.BigDecimal

/**
 * FiatValue only available when type == Voucher
 * As of right now the only time fiat value is different from null is with Vouchers because,
 * otherwise we are only using APPC as the base value.
 */
data class PaymentMethodsData(val appPackage: String, val isBds: Boolean,
                              val developerPayload: String?, val uri: String?,
                              val transactionValue: BigDecimal, val fiatValue: FiatValue?)