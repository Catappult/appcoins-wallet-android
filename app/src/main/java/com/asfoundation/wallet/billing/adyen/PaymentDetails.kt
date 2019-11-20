package com.asfoundation.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod

data class PaymentDetails(val reference: String, val paymentMethod: PaymentMethod,
                          val returnUrl: String)