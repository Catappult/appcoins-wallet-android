package com.asfoundation.wallet.topup

import com.asfoundation.wallet.billing.adyen.PaymentType

data class PaymentTypeInfo(val paymentType: PaymentType, val paymentId: String)
