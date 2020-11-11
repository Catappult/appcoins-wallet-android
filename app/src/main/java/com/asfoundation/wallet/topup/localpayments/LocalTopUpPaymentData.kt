package com.asfoundation.wallet.topup.localpayments

import com.asfoundation.wallet.topup.TopUpPaymentData

data class LocalTopUpPaymentData(val paymentId: String, val paymentIcon: String,
                                 val paymentLabel: String, val async: Boolean,
                                 val packageName: String, val topUpData: TopUpPaymentData)