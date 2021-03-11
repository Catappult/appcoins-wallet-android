package com.asfoundation.wallet.billing.adyen

import com.asfoundation.wallet.ui.iab.TransactionPaymentData

data class AdyenPaymentData(val returnUrl: String, val paymentType: String, val bonus: String,
                            val isPreselected: Boolean, val gamificationLevel: Int,
                            val paymentData: TransactionPaymentData)
