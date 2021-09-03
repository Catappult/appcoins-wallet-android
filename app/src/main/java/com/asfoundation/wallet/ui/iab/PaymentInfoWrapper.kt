package com.asfoundation.wallet.ui.iab

data class PaymentInfoWrapper(val packageName: String, val skuDetails: String?, val value: String,
                              val purchaseDetails: String, val transactionType: String)