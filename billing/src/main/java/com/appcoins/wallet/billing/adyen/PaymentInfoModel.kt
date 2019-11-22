package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod

data class PaymentInfoModel(
    val paymentMethodInfo: PaymentMethod?,
    val error: Error = Error())

data class Error(val hasError: Boolean = false, val isNetworkError: Boolean = false)