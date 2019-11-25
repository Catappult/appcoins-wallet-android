package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.util.Error

data class PaymentInfoModel(
    val paymentMethodInfo: PaymentMethod?,
    val error: Error = Error())
