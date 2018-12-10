package com.asfoundation.wallet.billing.purchase

import com.asfoundation.wallet.billing.BillingService

interface BillingFactory {
    fun getBilling(merchantName: String): BillingService
}
