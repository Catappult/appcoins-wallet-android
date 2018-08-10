package com.appcoins.wallet.billing

interface BillingFactory {
  fun getBilling(merchantName: String): Billing
}
