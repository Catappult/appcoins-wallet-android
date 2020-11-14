package com.asfoundation.wallet.ui.iab.payments.carrier.status

data class CarrierPaymentData(val domain: String, val transactionData: String,
                              val transactionType: String, val paymentUrl: String)