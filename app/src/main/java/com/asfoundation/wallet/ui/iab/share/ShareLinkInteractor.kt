package com.asfoundation.wallet.ui.iab.share

import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.appcoins.wallet.feature.walletInfo.data.wallet.FindDefaultWalletInteract
import io.reactivex.Single
import javax.inject.Inject

class ShareLinkInteractor @Inject constructor(private val remoteRepository: ShareLinkRepository,
                                              private val walletInteractor: FindDefaultWalletInteract,
                                              private val inAppPurchaseInteractor: InAppPurchaseInteractor) {

  fun getLinkToShare(domain: String, skuId: String?, message: String?,
                     originalAmount: String?, originalCurrency: String?,
                     paymentMethod: String): Single<String> {
    return walletInteractor.find()
        .flatMap {
          remoteRepository.getLink(domain, skuId, message, it.address, originalAmount,
              originalCurrency, paymentMethod)
        }
  }

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

}
