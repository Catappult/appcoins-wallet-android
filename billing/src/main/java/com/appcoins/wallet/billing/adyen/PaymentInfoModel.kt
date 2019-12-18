package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.util.Error

data class PaymentInfoModel(
    val paymentMethodInfo: PaymentMethod?,
    val isStored: Boolean = false,
    val price: Price,
    val error: Error = Error()) {

  constructor(error: Error) : this(null, false, Price("0", ""), error)
}
