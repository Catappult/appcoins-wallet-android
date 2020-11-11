package com.asfoundation.wallet.ui.iab.local_payments

import java.math.BigDecimal

data class LocalPaymentData(val packageName: String, val skuId: String?,
                            val originalAmount: String?, val currency: String?,
                            val bonus: String?, val paymentId: String,
                            val developerAddress: String, val type: String,
                            val amount: BigDecimal, val callbackUrl: String?,
                            val orderReference: String?, val payload: String?,
                            val paymentMethodIconUrl: String?, val label: String?,
                            val async: Boolean, val gamificationLevel: Int)
