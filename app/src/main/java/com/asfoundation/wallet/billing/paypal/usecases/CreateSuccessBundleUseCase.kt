package com.asfoundation.wallet.billing.paypal.usecases

import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject

class CreateSuccessBundleUseCase @Inject constructor(
  val inAppPurchaseInteractor: InAppPurchaseInteractor
) {

  operator fun invoke(type: String, merchantName: String, sku: String?,
                                purchaseUid: String?, orderReference: String?, hash: String?,
                                scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    return inAppPurchaseInteractor.getCompletedPurchaseBundle(type, merchantName, sku, purchaseUid,
      orderReference, hash, scheduler)
  }

}
