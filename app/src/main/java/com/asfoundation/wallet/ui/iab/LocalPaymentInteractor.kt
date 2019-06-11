package com.asfoundation.wallet.ui.iab

import android.net.Uri
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.*
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Observable
import io.reactivex.Single

class LocalPaymentInteractor(private val remoteRepository: ShareLinkRepository,
                             private val walletInteractor: FindDefaultWalletInteract,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor
) {

  fun getPaymentLink(domain: String, skuId: String?,
                     originalAmount: String?, originalCurrency: String?,
                     paymentMethod: String): Single<String> {

    return walletInteractor.find()
        .flatMap {
          remoteRepository.getLink(domain, skuId, null, it.address, originalAmount,
              originalCurrency, paymentMethod)
        }
  }

  fun getTransaction(uri: Uri): Observable<Transaction.Status> {
    return inAppPurchaseInteractor.getTransaction(uri.lastPathSegment)
        .map { it.status }
        .filter {
          isEndingState(it)
        }
        .distinctUntilChanged()
  }

  private fun isEndingState(status: Transaction.Status): Boolean {
    return status == PENDING_USER_PAYMENT || status == COMPLETED || status == FAILED || status == CANCELED || status == INVALID_TRANSACTION
  }
}
