package com.asfoundation.wallet.billing.purchase

import io.reactivex.Single

interface InAppDeepLinkRepository {

  /**
   * All optional fields should be passed despite possible being null as these are
   * required by some applications to complete the purchase flow
   */
  fun getDeepLink(domain: String, skuId: String?, userWalletAddress: String,
                  signature: String,
                  originalAmount: String?, originalCurrency: String?,
                  paymentMethod: String, developerWalletAddress: String,
                  storeWalletAddress: String, oemWalletAddress: String,
                  callbackUrl: String?, orderReference: String?,
                  payload: String?): Single<String>

}
