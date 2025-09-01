package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.microservices.model.CreditCardCVCResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.google.gson.JsonObject
import com.wallet.appcoins.feature.support.data.SupportInteractor
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
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val rxSchedulers: RxSchedulers
) {

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified(verificationType: VerificationType) =
    walletService.getAndSignCurrentWalletAddress()
      .flatMap {
        walletVerificationInteractor.isVerified(
          address = it.address,
          signature = it.signedAddress,
          type = verificationType
        )
      }
      .onErrorReturn { true }


  fun showSupport(uid: String): Completable {
    return supportInteractor.showSupport(uid)
  }

  fun loadPaymentInfo(
    methods: AdyenPaymentRepository.Methods,
    value: String,
    currency: String
  ): Single<PaymentInfoModel> {
    return walletService.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMap { wallet ->
        adyenPaymentRepository
          .loadPaymentInfo(
            methods = methods,
            value = value,
            currency = currency,
            walletAddress = wallet,
          )
      }
  }

  fun makePayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    hasCvc: Boolean,
    supportedShopperInteraction: List<String>,
    returnUrl: String,
    value: String,
    currency: String,
    reference: String?,
    paymentType: String,
    origin: String?,
    packageName: String,
    metadata: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    referrerUrl: String?,
    guestWalletId: String?,
    externalBuyerReference: String?,
    isFreeTrial: Boolean?
  ): Single<PaymentModel> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress(),
      partnerAddressService.getAttribution(packageName),
    ) { address, attributionEntity -> address to attributionEntity }
      .flatMap { (addressModel, attrEntity) ->
        getCurrentPromoCodeUseCase()
          .flatMap { promoCode ->
            adyenPaymentRepository.makePayment(
              adyenPaymentMethod = adyenPaymentMethod,
              shouldStoreMethod = shouldStoreMethod,
              hasCvc = hasCvc,
              supportedShopperInteractions = supportedShopperInteraction,
              returnUrl = returnUrl,
              value = value,
              currency = currency,
              reference = reference,
              paymentType = paymentType,
              walletAddress = addressModel.address,
              origin = origin,
              packageName = packageName,
              metadata = metadata,
              sku = sku,
              callbackUrl = callbackUrl,
              transactionType = transactionType,
              entityOemId = attrEntity.oemId,
              entityDomain = attrEntity.domain,
              entityPromoCode = promoCode.code,
              userWallet = addressModel.address,
              referrerUrl = referrerUrl,
              guestWalletId = guestWalletId,
              externalBuyerReference = externalBuyerReference,
              isFreeTrial = isFreeTrial
            )
          }
      }
  }

  fun addCard(
    adyenPaymentMethod: ModelObject,
    hasCvc: Boolean,
    supportedShopperInteraction: List<String>,
    returnUrl: String,
    value: String,
    currency: String
  ): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap {
        val addressModel = it
        adyenPaymentRepository.makePayment(
          adyenPaymentMethod = adyenPaymentMethod,
          shouldStoreMethod = true,
          hasCvc = hasCvc,
          supportedShopperInteractions = supportedShopperInteraction,
          returnUrl = returnUrl,
          value = value,
          currency = currency,
          reference = null,
          paymentType = "credit_card",
          walletAddress = addressModel.address,
          origin = null,
          packageName = "com.appcoins.wallet",  // necessary for the verification request
          metadata = null,
          sku = null,
          callbackUrl = null,
          transactionType = "VERIFICATION",
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          referrerUrl = null,
          guestWalletId = null,
          externalBuyerReference = null,
          isFreeTrial = null
        )
      }
  }

  fun makeTopUpPayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    hasCvc: Boolean,
    supportedShopperInteraction: List<String>,
    returnUrl: String,
    value: String,
    currency: String,
    paymentType: String,
    transactionType: String,
    packageName: String
  ): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap {
        adyenPaymentRepository.makePayment(
          adyenPaymentMethod = adyenPaymentMethod,
          shouldStoreMethod = shouldStoreMethod,
          hasCvc = hasCvc,
          supportedShopperInteractions = supportedShopperInteraction,
          returnUrl = returnUrl,
          value = value,
          currency = currency,
          reference = null,
          paymentType = paymentType,
          walletAddress = it.address,
          origin = null,
          packageName = packageName,
          metadata = null,
          sku = null,
          callbackUrl = null,
          transactionType = transactionType,
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          referrerUrl = null,
          guestWalletId = null,
          externalBuyerReference = null,
          isFreeTrial = null
        )
      }
  }

  fun submitRedirect(
    uid: String,
    details: JsonObject,
    paymentData: String?
  ): Single<PaymentModel> {
    return walletService.getWalletAddress()
      .flatMap {
        adyenPaymentRepository.submitRedirect(
          uid, it, details,
          paymentData
        )
      }
  }

  fun disablePayments(): Single<Boolean> {
    return walletService.getWalletAddress()
      .flatMap {
        adyenPaymentRepository.setMandatoryCVC(false)
        adyenPaymentRepository.disablePayments(it)
      }
  }

  fun convertToFiat(amount: Double, currency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(amount, currency)
  }

  fun mapCancellation(): Bundle = billingMessagesMapper.mapCancellation()

  fun removePreSelectedPaymentMethod() = inAppPurchaseInteractor.removePreSelectedPaymentMethod()

  fun getCompletePurchaseBundle(
    type: String,
    merchantName: String,
    sku: String?,
    purchaseUid: String?,
    orderReference: String?,
    hash: String?,
    scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    return inAppPurchaseInteractor.getCompletedPurchaseBundle(
      type,
      merchantName,
      sku,
      purchaseUid,
      orderReference,
      hash,
      scheduler
    )
  }

  fun convertToLocalFiat(doubleValue: Double): Single<FiatValue> {
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
          Single.zip(
            adyenPaymentRepository.getTransaction(
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

  fun getCreditCardNeedCVC(): Single<CreditCardCVCResponse> {
    return adyenPaymentRepository.getCreditCardNeedCVC().subscribeOn(rxSchedulers.io)
  }

  fun setMandatoryCVC(mandatoryCvc: Boolean) {
    adyenPaymentRepository.setMandatoryCVC(mandatoryCvc)
  }

  companion object {
    private const val MAX_NUMBER_OF_TRIES = 5
    private const val REQUEST_INTERVAL_IN_SECONDS: Long = 2
    const val HIGH_AMOUNT_CHECK_ID = 63
    const val PAYMENT_METHOD_CHECK_ID = 73
  }
}
