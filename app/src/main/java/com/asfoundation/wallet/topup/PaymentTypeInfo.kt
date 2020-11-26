package com.asfoundation.wallet.topup

import com.asfoundation.wallet.billing.adyen.PaymentType
import java.io.Serializable

data class PaymentTypeInfo(val paymentType: PaymentType, val paymentId: String, val label: String,
                           val icon: String, val async: Boolean = false) : Serializable
