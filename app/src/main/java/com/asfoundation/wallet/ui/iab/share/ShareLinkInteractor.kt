package com.asfoundation.wallet.ui.iab.share

import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Single

class ShareLinkInteractor(private val remoteRepository: ShareLinkRepository,
                          private val walletInteractor: FindDefaultWalletInteract) {

  fun getLinkToShare(domain: String, skuId: String?, message: String?,
                     originalAmount: String?, originalCurrency: String?): Single<String> {
    return walletInteractor.find()
        .flatMap {
          remoteRepository.getLink(domain, skuId, message, it.address, originalAmount,
              originalCurrency)
        }
  }

}
