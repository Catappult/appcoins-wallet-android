package com.asfoundation.wallet.ui.iab.localpayments

import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.*
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class LocalPaymentInteractor(private val deepLinkRepository: InAppDeepLinkRepository,
                             private val walletService: WalletService,
                             private val partnerAddressService: AddressService,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                             private val billing: Billing,
                             private val billingMessagesMapper: BillingMessagesMapper,
                             private val supportInteractor: SupportInteractor,
                             private val walletBlockedInteract: WalletBlockedInteract,
                             private val smsValidationInteract: SmsValidationInteract,
                             private val remoteRepository: RemoteRepository) {

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
      walletService.getWalletAddress()
          .flatMap { smsValidationInteract.isValidated(it) }
          .onErrorReturn { true }

  fun getPaymentLink(domain: String, skuId: String?, originalAmount: String?,
                     originalCurrency: String?, paymentMethod: String, developerAddress: String,
                     callbackUrl: String?, orderReference: String?,
                     payload: String?): Single<String> {

    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          Single.zip(
              partnerAddressService.getStoreAddressForPackage(domain),
              partnerAddressService.getOemAddressForPackage(domain),
              BiFunction { storeAddress: String, oemAddress: String ->
                DeepLinkInformation(storeAddress, oemAddress)
              })
              .flatMap {
                deepLinkRepository.getDeepLink(domain, skuId, walletAddressModel.address,
                    walletAddressModel.signedAddress, originalAmount, originalCurrency,
                    paymentMethod, developerAddress, it.storeAddress, it.oemAddress, callbackUrl,
                    orderReference, payload)
              }
        }
  }

  fun getTopUpPaymentLink(packageName: String, fiatAmount: String,
                          fiatCurrency: String, paymentMethod: String,
                          productName: String): Single<String> {

    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          remoteRepository.createLocalPaymentTopUpTransaction(paymentMethod, packageName,
              fiatAmount, fiatCurrency, productName, walletAddressModel.address,
              walletAddressModel.signedAddress)
        }
        .map { it.url ?: "" }
  }

  fun getTransaction(uri: Uri, async: Boolean): Observable<Transaction> =
      inAppPurchaseInteractor.getTransaction(uri.lastPathSegment)
          .filter { isEndingState(it.status, async) }
          .distinctUntilChanged { transaction -> transaction.status }

  private fun isEndingState(status: Transaction.Status, async: Boolean) =
      (status == PENDING_USER_PAYMENT && async) ||
          status == COMPLETED ||
          status == FAILED ||
          status == CANCELED ||
          status == INVALID_TRANSACTION

  fun getCompletePurchaseBundle(type: String, merchantName: String, sku: String?,
                                orderReference: String?, hash: String?,
                                scheduler: Scheduler): Single<Bundle> =
      if (isInApp(type) && sku != null) {
        billing.getSkuPurchase(merchantName, sku, scheduler)
            .map { billingMessagesMapper.mapPurchase(it, orderReference) }
      } else {
        Single.just(billingMessagesMapper.successBundle(hash))
      }

  private fun isInApp(type: String) = type.equals("INAPP", ignoreCase = true)

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

  fun saveAsyncLocalPayment(paymentMethod: String) {
    inAppPurchaseInteractor.saveAsyncLocalPayment(paymentMethod)
  }

  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun topUpBundle(priceAmount: String, priceCurrency: String, bonus: String,
                  fiatCurrencySymbol: String): Bundle {
    return billingMessagesMapper.topUpBundle(priceAmount, priceCurrency, bonus, fiatCurrencySymbol)
  }

  fun convertToFiat(appcAmount: Double, toCurrency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(appcAmount, toCurrency)
  }

  private data class DeepLinkInformation(val storeAddress: String, val oemAddress: String)
}
