package com.asfoundation.wallet.ui.iab.localpayments

import java.math.BigDecimal

data class LocalPaymentData(val packageName: String, val skuId: String?, val fiatAmount: String?,
                            val currency: String?, val bonus: String?, val paymentId: String,
                            val developerAddress: String, val type: String,
                            val appcAmount: BigDecimal, val callbackUrl: String?,
                            val orderReference: String?, val payload: String?, val origin: String?,
                            val paymentMethodIconUrl: String?, val label: String?,
                            val async: Boolean, val referrerUrl: String?,
                            val gamificationLevel: Int)
