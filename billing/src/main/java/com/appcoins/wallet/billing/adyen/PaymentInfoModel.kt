package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.util.Error
import java.math.BigDecimal

data class PaymentInfoModel(
    val paymentMethodInfo: PaymentMethod?,
    val isStored: Boolean = false,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val error: Error = Error()) {

  constructor(error: Error) : this(null, false, BigDecimal.ZERO, "", error)
}
