package com.asfoundation.wallet.repository

data class PaymentError(val paymentState: PaymentTransaction.PaymentState,
                        val errorCode: Int? = null, val errorMessage: String? = null)