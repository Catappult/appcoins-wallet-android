package com.asfoundation.wallet.billing.address

import java.math.BigDecimal


data class BillingAddressData(val skuId: String, val skuDescription: String,
                              val transactionType: String, val domain: String, val bonus: String?,
                              val appcAmount: BigDecimal, val fiatAmount: BigDecimal,
                              val fiatCurrency: String, val isDonation: Boolean,
                              val shouldStoreCard: Boolean, val isStored: Boolean)