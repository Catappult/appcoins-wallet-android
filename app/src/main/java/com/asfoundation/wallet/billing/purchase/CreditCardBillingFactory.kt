package com.asfoundation.wallet.billing.purchase

import com.asfoundation.wallet.billing.CreditCardBilling

interface CreditCardBillingFactory {
  fun getBilling(merchantName: String): CreditCardBilling
}
