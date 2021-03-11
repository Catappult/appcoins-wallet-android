package com.asfoundation.wallet.ui.iab

import java.io.Serializable
import java.math.BigDecimal

data class TransactionPaymentData(val fiatAmount: BigDecimal, val currency: String,
                                  val isBds: Boolean, val type: String, val domain: String,
                                  val origin: String?, val appcAmount: BigDecimal,
                                  val skuId: String?, val skuDescription: String,
                                  val payload: String?, val callbackUrl: String?,
                                  val toAddress: String?, val referrerUrl: String?,
                                  val orderReference: String?) : Serializable
