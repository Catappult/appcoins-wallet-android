package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.verification.WalletVerificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdyenPaymentInteractor(private val adyenPaymentRepository: AdyenPaymentRepository,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                             private val billingMessagesMapper: BillingMessagesMapper,
                             private val partnerAddressService: AddressService,
                             private val billing: Billing,
                             private val walletService: WalletService,
                             private val supportInteractor: SupportInteractor,
                             private val walletBlockedInteract: WalletBlockedInteract,
                             private val walletVerificationInteractor: WalletVerificationInteractor,
                             private val billingAddressRepository: BillingAddressRepository
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

  fun loadPaymentInfo(methods: AdyenPaymentRepository.Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return walletService.getWalletAddress()
        .flatMap { adyenPaymentRepository.loadPaymentInfo(methods, value, currency, it) }
  }

  fun makePayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
                  supportedShopperInteraction: List<String>,
                  returnUrl: String, value: String, currency: String, reference: String?,
                  paymentType: String, origin: String?, packageName: String, metadata: String?,
                  sku: String?, callbackUrl: String?, transactionType: String,
                  developerWallet: String?,
                  referrerUrl: String?,
                  billingAddress: AdyenBillingAddress? = null): Single<PaymentModel> {
    return Single.zip(walletService.getAndSignCurrentWalletAddress(),
        partnerAddressService.getAttributionEntity(packageName),
        { address, attributionEntity -> Pair(address, attributionEntity) })
        .flatMap { pair ->
          val addressModel = pair.first
          val attrEntity = pair.second
          adyenPaymentRepository.makePayment(adyenPaymentMethod, shouldStoreMethod, hasCvc,
              supportedShopperInteraction, returnUrl, value, currency, reference, paymentType,
              addressModel.address, origin, packageName, metadata, sku, callbackUrl,
              transactionType, developerWallet, attrEntity.oemId, attrEntity.domain,
              addressModel.address,
              addressModel.signedAddress, billingAddress, referrerUrl)

        }
  }

  fun makeTopUpPayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
                       supportedShopperInteraction: List<String>, returnUrl: String, value: String,
                       currency: String, paymentType: String, transactionType: String,
                       packageName: String,
                       billingAddress: AdyenBillingAddress? = null): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          adyenPaymentRepository.makePayment(adyenPaymentMethod, shouldStoreMethod, hasCvc,
              supportedShopperInteraction, returnUrl, value, currency, null, paymentType,
              it.address, null, packageName, null, null, null, transactionType, null, null, null,
              null, it.signedAddress, billingAddress, null)
        }
  }

  fun submitRedirect(uid: String, details: JSONObject,
                     paymentData: String?): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          adyenPaymentRepository.submitRedirect(uid, it.address, it.signedAddress, details,
              paymentData)
        }
  }

  fun disablePayments(): Single<Boolean> {
    return walletService.getWalletAddress()
        .flatMap { adyenPaymentRepository.disablePayments(it) }
  }

  fun convertToFiat(amount: Double, currency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(amount, currency)
  }

  fun mapCancellation(): Bundle {
    return billingMessagesMapper.mapCancellation()
  }

  fun removePreSelectedPaymentMethod() {
    inAppPurchaseInteractor.removePreSelectedPaymentMethod()
  }

  fun getCompletePurchaseBundle(type: String, merchantName: String, sku: String?,
                                orderReference: String?, hash: String?,
                                scheduler: Scheduler): Single<Bundle> {
    return if (isInApp(type) && sku != null) {
      billing.getSkuPurchase(merchantName, sku, scheduler)
          .map { billingMessagesMapper.mapPurchase(it, orderReference) }
    } else {
      Single.just(billingMessagesMapper.successBundle(hash))
    }
  }

  fun convertToLocalFiat(doubleValue: Double): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToLocalFiat(doubleValue)
  }

  fun getAuthorisedTransaction(uid: String): Observable<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapObservable { walletAddressModel ->
          Observable.interval(0, 10, TimeUnit.SECONDS, Schedulers.io())
              .timeInterval()
              .switchMap {
                adyenPaymentRepository.getTransaction(uid, walletAddressModel.address,
                    walletAddressModel.signedAddress)
                    .toObservable()
              }
              .filter { isEndingState(it.status) }
              .distinctUntilChanged { transaction -> transaction.status }
        }
  }

  fun getFailedTransactionReason(uid: String, timesCalled: Int = 0): Single<PaymentModel> {
    return if (timesCalled < MAX_NUMBER_OF_TRIES) {
      walletService.getAndSignCurrentWalletAddress()
          .flatMap { walletAddressModel ->
            Single.zip(adyenPaymentRepository.getTransaction(uid, walletAddressModel.address,
                walletAddressModel.signedAddress),
                Single.timer(REQUEST_INTERVAL_IN_SECONDS, TimeUnit.SECONDS),
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

  private fun isEndingState(status: TransactionStatus): Boolean {
    return (status == TransactionStatus.COMPLETED
        || status == TransactionStatus.FAILED
        || status == TransactionStatus.CANCELED
        || status == TransactionStatus.INVALID_TRANSACTION
        || status == TransactionStatus.FRAUD)
  }

  private fun isInApp(type: String): Boolean {
    return type.equals("INAPP", ignoreCase = true)
  }

  companion object {
    private const val MAX_NUMBER_OF_TRIES = 5
    private const val REQUEST_INTERVAL_IN_SECONDS: Long = 2
    const val HIGH_AMOUNT_CHECK_ID = 63
    const val PAYMENT_METHOD_CHECK_ID = 73
  }
}
