package com.asfoundation.wallet.billing.share

import com.appcoins.wallet.core.network.microservices.DeeplinkApiModule
import com.appcoins.wallet.core.network.microservices.model.ShareLinkData
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = ShareLinkRepository::class)
class BdsShareLinkRepository @Inject constructor(private var api: DeeplinkApiModule.BdsShareLinkApi) :
  ShareLinkRepository {

  override fun getLink(
    domain: String, skuId: String?, message: String?, walletAddress: String,
    originalAmount: String?, originalCurrency: String?,
    paymentMethod: String
  ): Single<String> {
    return api.getPaymentLink(
      ShareLinkData(
        domain, skuId, walletAddress, message, originalAmount, originalCurrency,
        paymentMethod
      )
    )
      .map { it.url }
  }
}
