package com.asfoundation.wallet.billing.share

import io.reactivex.Single

interface ShareLinkRepository {

  fun getLink(domain: String, skuId: String?, message: String?,
              walletAddress: String,
              originalAmount: String?,
              originalCurrency: String?): Single<String>
}
