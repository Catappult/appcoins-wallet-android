package com.asfoundation.wallet.ui.iab.localpayments

import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.Transaction.Status
import com.appcoins.wallet.core.network.microservices.model.Transaction.Status.*
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject

class LocalPaymentInteractor @Inject constructor(
  private val walletService: WalletService,
  private val partnerAddressService: AddressService,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val billingMessagesMapper: BillingMessagesMapper,
  private val supportInteractor: SupportInteractor,
  private val walletBlockedInteract: WalletBlockedInteract,
  private val walletVerificationInteractor: WalletVerificationInteractor,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val remoteRepository: RemoteRepository
) {

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
    walletService.getAndSignCurrentWalletAddress()
      .flatMap { walletVerificationInteractor.isVerified(it.address, it.signedAddress) }
      .onErrorReturn { true }

  fun getPaymentLink(
    paymentMethod: String, packageName: String, fiatAmount: String?, fiatCurrency: String?,
    productName: String?, type: String, origin: String?,
    walletDeveloper: String?, developerPayload: String?,
    callbackUrl: String?, orderReference: String?,
    referrerUrl: String?
  ): Single<String> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        partnerAddressService.getAttributionEntity(packageName)
          .flatMap { attributionEntity ->
            getCurrentPromoCodeUseCase().flatMap { promoCode ->
              remoteRepository.createLocalPaymentTransaction(
                paymentMethod, packageName,
                fiatAmount, fiatCurrency, productName, type, origin, walletDeveloper,
                attributionEntity.oemId, attributionEntity.domain, promoCode.code,
                developerPayload,
                callbackUrl, orderReference,
                referrerUrl, address
              )
            }
          }
          .map { it.url }
      }
  }

  fun getTopUpPaymentLink(
    packageName: String, fiatAmount: String,
    fiatCurrency: String, paymentMethod: String,
    productName: String
  ): Single<String> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        remoteRepository.createLocalPaymentTransaction(
          paymentMethod, packageName,
          fiatAmount, fiatCurrency, productName, TOP_UP_TRANSACTION_TYPE,
          null, null, null, null, null, null, null, null, null,
          address
        )
      }
      .map { it.url }
  }

  fun getTransaction(uri: Uri, async: Boolean): Observable<Transaction> =
    inAppPurchaseInteractor.getTransaction(uri.lastPathSegment)
      .filter { isEndingState(it.status, async) }
      .distinctUntilChanged { transaction -> transaction.status }

  private fun isEndingState(status: Status, async: Boolean) =
    (status == PENDING_USER_PAYMENT && async) ||
        status == COMPLETED ||
        status == FAILED ||
        status == CANCELED ||
        status == INVALID_TRANSACTION

  fun getCompletePurchaseBundle(
    type: String, merchantName: String, sku: String?,
    purchaseUid: String?,
    orderReference: String?, hash: String?,
    scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    return inAppPurchaseInteractor.getCompletedPurchaseBundle(
      type, merchantName, sku, purchaseUid,
      orderReference, hash, scheduler
    )
  }

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

  fun saveAsyncLocalPayment(paymentMethod: String) {
    inAppPurchaseInteractor.saveAsyncLocalPayment(paymentMethod)
  }

  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun topUpBundle(
    priceAmount: String, priceCurrency: String, bonus: String,
    fiatCurrencySymbol: String
  ): Bundle {
    return billingMessagesMapper.topUpBundle(priceAmount, priceCurrency, bonus, fiatCurrencySymbol)
  }

  fun convertToFiat(appcAmount: Double, toCurrency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(appcAmount, toCurrency)
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
    private const val INAPP_TRANSACTION_TYPE = "INAPP"
  }

  data class StoreOemAddresses(val storeAddress: String, val oemAddress: String)
}
