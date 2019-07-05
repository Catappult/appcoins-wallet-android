package com.asfoundation.wallet.ui.iab

import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.*
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class LocalPaymentInteractor(private val deepLinkRepository: InAppDeepLinkRepository,
                             private val walletService: WalletService,
                             private val partnerAddressService: AddressService,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                             private val billing: Billing,
                             private val billingMessagesMapper: BillingMessagesMapper
) {

  fun getPaymentLink(domain: String, skuId: String?,
                     originalAmount: String?, originalCurrency: String?,
                     paymentMethod: String, developerAddress: String): Single<String> {

    return walletService.getWalletAddress()
        .flatMap { address ->
          Single.zip(
              walletService.signContent(address),
              partnerAddressService.getStoreAddressForPackage(domain),
              BiFunction { signature: String, storeAddress: String ->
                Pair(signature, storeAddress)
              })
              .flatMap {
                deepLinkRepository.getDeepLink(domain, skuId, address, it.first, originalAmount,
                    originalCurrency, paymentMethod, developerAddress, it.second)
              }
        }
  }

  fun getTransaction(uri: Uri): Observable<Transaction> {
    return inAppPurchaseInteractor.getTransaction(uri.lastPathSegment)
        .filter {
          isEndingState(it.status, it.type)
        }
        .distinctUntilChanged { transaction -> transaction.status }
  }

  private fun isEndingState(status: Transaction.Status, type: String): Boolean {
    return (status == PENDING_USER_PAYMENT && type == "TOPUP") || (status == COMPLETED && (type == "INAPP" || type == "INAPP_UNMANAGED")) || status == FAILED || status == CANCELED || status == INVALID_TRANSACTION
  }

  fun getCompletePurchaseBundle(isInApp: Boolean, merchantName: String, sku: String?,
                                scheduler: Scheduler,
                                orderReference: String?, hash: String?): Single<Bundle> {
    return if (isInApp && sku != null) {
      billing.getSkuPurchase(merchantName, sku, scheduler)
          .map {
            billingMessagesMapper.mapPurchase(it,
                orderReference)
          }
    } else {
      Single.just(billingMessagesMapper.successBundle(hash))
    }
  }
}
