package com.appcoins.wallet.bdsbilling

interface BillingFactory {
  fun getBilling(merchantName: String): Billing
}
