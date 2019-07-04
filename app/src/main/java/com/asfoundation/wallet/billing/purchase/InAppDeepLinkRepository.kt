package com.asfoundation.wallet.billing.purchase

import io.reactivex.Single

interface InAppDeepLinkRepository {

  fun getDeepLink(domain: String, skuId: String?, userWalletAddress: String, signature: String,
                  originalAmount: String?, originalCurrency: String?,
                  paymentMethod: String, developerWalletAddress: String): Single<String>


}
