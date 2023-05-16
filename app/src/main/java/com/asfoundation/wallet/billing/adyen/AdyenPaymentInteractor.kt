package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.core.network.microservices.model.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.support.SupportInteractor
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdyenPaymentInteractor @Inject constructor(
  private val adyenPaymentRepository: AdyenPaymentRepository,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val billingMessagesMapper: BillingMessagesMapper,
  private val partnerAddressService: AddressService,
  private val walletService: WalletService,
  private val supportInteractor: SupportInteractor,
  private val walletBlockedInteract: WalletBlockedInteract,
  private val walletVerificationInteractor: WalletVerificationInteractor,
  private val billingAddressRepository: BillingAddressRepository,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val rxSchedulers: RxSchedulers
) {

  fun forgetBillingAddress() = billingAddressRepository.forgetBillingAddress()

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
    walletService.getAndSignCurrentWalletAddress()
      .flatMap { walletVerificationInteractor.isVerified(it.address, it.signedAddress) }
      .onErrorReturn { true }


  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun loadPaymentInfo(
    methods: AdyenPaymentRepository.Methods, value: String,
    currency: String
  ): Single<PaymentInfoModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap {
        adyenPaymentRepository
          .loadPaymentInfo(methods, value, currency, it.address, it.signedAddress)
      }
  }

  fun makePayment(
    adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
    supportedShopperInteraction: List<String>,
    returnUrl: String, value: String, currency: String, reference: String?,
    paymentType: String, origin: String?, packageName: String, metadata: String?,
    sku: String?, callbackUrl: String?, transactionType: String,
    developerWallet: String?,
    referrerUrl: String?,
    billingAddress: AdyenBillingAddress? = null
  ): Single<PaymentModel> {
    return Single.zip(walletService.getAndSignCurrentWalletAddress(),
      partnerAddressService.getAttributionEntity(packageName),
      { address, attributionEntity -> Pair(address, attributionEntity) })
      .flatMap { pair ->
        val addressModel = pair.first
        val attrEntity = pair.second
        getCurrentPromoCodeUseCase().flatMap { promoCode ->
          adyenPaymentRepository.makePayment(
            adyenPaymentMethod, shouldStoreMethod, hasCvc,
            supportedShopperInteraction, returnUrl, value, currency, reference, paymentType,
            addressModel.address, origin, packageName, metadata, sku, callbackUrl,
            transactionType, developerWallet, attrEntity.oemId, attrEntity.domain,
            promoCode.code,
            addressModel.address,
            addressModel.signedAddress, billingAddress, referrerUrl
          )
        }
      }
  }

  fun makeTopUpPayment(
    adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
    supportedShopperInteraction: List<String>, returnUrl: String, value: String,
    currency: String, paymentType: String, transactionType: String,
    packageName: String,
    billingAddress: AdyenBillingAddress? = null
  ): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap {
        adyenPaymentRepository.makePayment(
          adyenPaymentMethod, shouldStoreMethod, hasCvc,
          supportedShopperInteraction, returnUrl, value, currency, null, paymentType,
          it.address, null, packageName, null, null, null, transactionType, null, null, null,
          null,
          null, it.signedAddress, billingAddress, null
        )
      }
  }

  fun submitRedirect(
    uid: String, details: JsonObject,
    paymentData: String?
  ): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap {
        adyenPaymentRepository.submitRedirect(
          uid, it.address, it.signedAddress, details,
          paymentData
        )
      }
  }

  fun disablePayments(): Single<Boolean> {
    return walletService.getWalletAddress()
      .flatMap { adyenPaymentRepository.disablePayments(it) }
  }

  fun convertToFiat(amount: Double, currency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(amount, currency)
  }

  fun mapCancellation(): Bundle = billingMessagesMapper.mapCancellation()

  fun removePreSelectedPaymentMethod() = inAppPurchaseInteractor.removePreSelectedPaymentMethod()

  fun getCompletePurchaseBundle(
    type: String, merchantName: String, sku: String?,
    purchaseUid: String?, orderReference: String?, hash: String?,
    scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    return inAppPurchaseInteractor.getCompletedPurchaseBundle(
      type, merchantName, sku, purchaseUid,
      orderReference, hash, scheduler
    )
  }

  fun convertToLocalFiat(doubleValue: Double): Single<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue> {
    return inAppPurchaseInteractor.convertToLocalFiat(doubleValue)
  }

  fun getAuthorisedTransaction(uid: String): Observable<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMapObservable { walletAddressModel ->
        Observable.interval(0, 5, TimeUnit.SECONDS, rxSchedulers.io)
          .timeInterval()
          .switchMap {
            adyenPaymentRepository.getTransaction(
              uid, walletAddressModel.address,
              walletAddressModel.signedAddress
            )
              .toObservable()
          }
          .filter { isEndingState(it.status) }
          .distinctUntilChanged { transaction -> transaction.status }
          .takeUntil { isEndingState(it.status) }
      }
  }

  fun getFailedTransactionReason(uid: String, timesCalled: Int = 0): Single<PaymentModel> {
    return if (timesCalled < MAX_NUMBER_OF_TRIES) {
      walletService.getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          Single.zip(adyenPaymentRepository.getTransaction(
            uid, walletAddressModel.address,
            walletAddressModel.signedAddress
          ),
            Single.timer(REQUEST_INTERVAL_IN_SECONDS, TimeUnit.SECONDS, rxSchedulers.io),
            BiFunction { paymentModel: PaymentModel, _: Long -> paymentModel })
        }
        .flatMap {
          if (it.errorCode != null) Single.just(it)
          else getFailedTransactionReason(it.uid, timesCalled + 1)
        }
    } else {
      Single.just(PaymentModel(Error(true)))
    }
  }

  fun getWalletAddress() = walletService.getWalletAddress()

  private fun isEndingState(status: PaymentModel.Status): Boolean {
    return (status == PaymentModel.Status.COMPLETED
        || status == PaymentModel.Status.FAILED
        || status == PaymentModel.Status.CANCELED
        || status == PaymentModel.Status.INVALID_TRANSACTION
        || status == PaymentModel.Status.FRAUD)
  }

  companion object {
    private const val MAX_NUMBER_OF_TRIES = 5
    private const val REQUEST_INTERVAL_IN_SECONDS: Long = 2
    const val HIGH_AMOUNT_CHECK_ID = 63
    const val PAYMENT_METHOD_CHECK_ID = 73
  }
}
