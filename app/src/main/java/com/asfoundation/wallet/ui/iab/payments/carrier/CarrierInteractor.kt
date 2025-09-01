package com.asfoundation.wallet.ui.iab.payments.carrier

import android.net.Uri
import com.appcoins.wallet.billing.carrierbilling.AvailableCountryListModel
import com.appcoins.wallet.billing.carrierbilling.CarrierPaymentModel
import com.appcoins.wallet.billing.carrierbilling.GenericError
import com.appcoins.wallet.billing.carrierbilling.NoError
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.TransactionStatus
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.billing.carrier_billing.CarrierBillingRepository
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletAddresses
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletStatus
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CarrierInteractor @Inject constructor(
  private val repository: CarrierBillingRepository,
  private val walletService: WalletService,
  private val partnerAddressService: AddressService,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val walletBlockedInteract: WalletBlockedInteract,
  private val walletVerificationInteractor: com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor,
  private val getCurrentPromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase,
  private val logger: Logger,
  private val rxSchedulers: RxSchedulers
) {

  fun createPayment(
    phoneNumber: String, packageName: String,
    origin: String?, transactionData: String, transactionType: String,
    currency: String,
    value: String
  ): Single<CarrierPaymentModel> {
    return Single.zip(
      getAddresses(packageName), getTransactionBuilder(transactionData)
    ) { addrs: WalletAddresses, builder: TransactionBuilder ->
      TransactionDataDetails(addrs, builder)
    }
      .flatMap { details ->
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          repository.makePayment(
            walletAddress = details.addrs.userAddress,
            phoneNumber = phoneNumber,
            packageName = packageName,
            origin = origin,
            sku = details.builder.skuId,
            reference = details.builder.orderReference,
            transactionType = transactionType,
            currency = currency,
            value = value,
            entityOemId = details.addrs.entityOemId,
            entityDomain = details.addrs.entityDomain,
            entityPromoCode = promoCode.code,
            userWallet = details.addrs.userAddress,
            referrerUrl = details.builder.referrerUrl,
            developerPayload = details.builder.payload,
            callbackUrl = details.builder.callbackUrl,
            guestWalletId = details.builder.guestWalletId
          )
        }
      }
      .doOnError { logger.log("CarrierInteractor", it) }
  }

  fun getFinishedPayment(uri: Uri, packageName: String): Single<CarrierPaymentModel> {
    return getAddresses(packageName)
      .flatMapObservable { addresses ->
        observeTransactionUpdates(getUidFromUri(uri)!!, addresses.userAddress)
      }
      .firstOrError()
      .map { paymentModel ->
        if (paymentModel.error == NoError && isErrorStatus(paymentModel.status)) {
          return@map paymentModel.copy(
            error = GenericError(false, -1, getErrorReasonFromUri(uri))
          )
        }
        return@map paymentModel
      }
  }

  private fun isErrorStatus(status: TransactionStatus) =
    status == TransactionStatus.FAILED ||
        status == TransactionStatus.CANCELED ||
        status == TransactionStatus.INVALID_TRANSACTION

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
      .map { bundle -> addPreselected(bundle) }
  }

  private fun addPreselected(purchaseBundle: PurchaseBundleModel): PurchaseBundleModel {
    val bundle = purchaseBundle.bundle
    bundle.putString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.CARRIER_BILLING.id
    )
    return PurchaseBundleModel(bundle, purchaseBundle.renewal)
  }

  fun removePreSelectedPaymentMethod() {
    inAppPurchaseInteractor.removePreSelectedPaymentMethod()
  }

  private fun getTransactionBuilder(transactionData: String): Single<TransactionBuilder> {
    return inAppPurchaseInteractor.parseTransaction(transactionData, true)
      .subscribeOn(rxSchedulers.io)
  }

  private fun getAddresses(packageName: String): Single<WalletAddresses> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress()
        .subscribeOn(rxSchedulers.io), partnerAddressService.getAttribution(packageName)
        .subscribeOn(rxSchedulers.io)
    ) { addressModel, attributionEntity ->
      WalletAddresses(
        addressModel.address, addressModel.signedAddress, attributionEntity.oemId,
        attributionEntity.domain
      )
    }
  }

  private fun observeTransactionUpdates(
    uid: String,
    walletAddress: String
  ): Observable<CarrierPaymentModel> {
    return Observable.interval(0, 5, TimeUnit.SECONDS, rxSchedulers.io)
      .timeInterval()
      .switchMap { repository.getPayment(uid, walletAddress) }
      .filter { paymentModel -> isEndingState(paymentModel.status) }
      .distinctUntilChanged { transaction -> transaction.status }
  }

  private fun isEndingState(status: TransactionStatus) =
    status == TransactionStatus.COMPLETED ||
        status == TransactionStatus.FAILED ||
        status == TransactionStatus.CANCELED ||
        status == TransactionStatus.INVALID_TRANSACTION

  private fun getUidFromUri(uri: Uri): String? {
    return uri.getQueryParameter("remote_txid")
  }

  private fun getErrorReasonFromUri(uri: Uri): String {
    return uri.getQueryParameter("why") ?: "Unknown Error"
  }

  fun convertToFiat(amount: Double, currency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(amount, currency)
  }

  fun getWalletStatus(): Single<WalletStatus> {
    return Single.zip(
      walletBlockedInteract.isWalletBlocked()
        .subscribeOn(rxSchedulers.io), isWalletVerified().subscribeOn(rxSchedulers.io),
      BiFunction { blocked, verified -> WalletStatus(blocked, verified) })
  }

  private fun isWalletVerified(): Single<Boolean> =
    walletService.getAndSignCurrentWalletAddress()
      .flatMap { walletVerificationInteractor.isAtLeastOneVerified(it.address) }
      .onErrorReturn { true }

  fun retrieveAvailableCountries(): Single<AvailableCountryListModel> {
    return repository.retrieveAvailableCountryList()
  }

  fun savePhoneNumber(phoneNumber: String): Completable {
    return Completable.fromAction { repository.savePhoneNumber(phoneNumber) }
  }

  fun forgetPhoneNumber() = repository.forgetPhoneNumber()

  fun retrievePhoneNumber() = repository.retrievePhoneNumber()
}

data class TransactionDataDetails(val addrs: WalletAddresses, val builder: TransactionBuilder)
