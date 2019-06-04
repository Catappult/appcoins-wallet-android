package com.asfoundation.wallet.ui.iab

import android.util.Log
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Single

class LocalPaymentInteractor(private val remoteRepository: ShareLinkRepository,
                             private val walletInteractor: FindDefaultWalletInteract) {
  fun getPaymentLink(domain: String, skuId: String?,
                     originalAmount: String?, originalCurrency: String?,
                     paymentMethod: String): Single<String> {

    return walletInteractor.find()
        .flatMap {
          Log.d("TAG123",
              domain + skuId + it.address + originalAmount + originalCurrency +
                  paymentMethod)
          remoteRepository.getLink(domain, skuId, null, it.address, originalAmount,
              originalCurrency, paymentMethod)
        }
  }
}
