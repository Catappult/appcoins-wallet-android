package com.appcoins.wallet.billing

internal interface BillingFactory {
  fun getBilling(merchantName: String): Billing
}
