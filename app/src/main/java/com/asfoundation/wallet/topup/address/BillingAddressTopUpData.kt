package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.topup.TopUpPaymentData

data class BillingAddressTopUpData(val data: TopUpPaymentData, val fiatAmount: String,
                                   val fiatCurrency: String, val shouldStoreCard: Boolean,
                                   val isStored: Boolean)