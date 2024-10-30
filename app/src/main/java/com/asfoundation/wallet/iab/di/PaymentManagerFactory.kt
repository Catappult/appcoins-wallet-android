package com.asfoundation.wallet.iab.di

import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import dagger.assisted.AssistedFactory

@AssistedFactory
interface PaymentManagerFactory {

  fun create(purchaseData: PurchaseData): PaymentManager
}
