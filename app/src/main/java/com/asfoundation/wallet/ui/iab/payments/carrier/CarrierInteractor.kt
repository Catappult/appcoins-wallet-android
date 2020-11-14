package com.asfoundation.wallet.ui.iab.payments.carrier

import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierPaymentModel
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletAddresses
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletStatus
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.util.concurrent.TimeUnit

class CarrierInteractor(private val repository: CarrierBillingRepository,
                        private val walletService: WalletService,
                        private val partnerAddressService: AddressService,
                        private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                        private val walletBlockedInteract: WalletBlockedInteract,
                        private val smsValidationInteract: SmsValidationInteract,
                        private val billing: Billing,
                        private val billingMessagesMapper: BillingMessagesMapper,
                        private val logger: Logger,
                        private val ioScheduler: Scheduler) {

  fun createPayment(phoneNumber: String, packageName: String,
                    origin: String?, transactionData: String, transactionType: String,
                    currency: String,
                    value: String): Single<CarrierPaymentModel> {
    return Single.zip(getAddresses(packageName), getTransactionBuilder(transactionData),
        BiFunction { addrs: WalletAddresses, builder: TransactionBuilder ->
          Pair(addrs, builder)
        })
        .flatMap { pair ->
          repository.makePayment(pair.first.address, pair.first.signedAddress, phoneNumber,
              packageName, origin, pair.second.skuId, pair.second.orderReference, transactionType,
              currency, value, pair.second.toAddress(), pair.first.oemAddress,
              pair.first.storeAddress, pair.first.address)
        }
        .doOnError { e -> logger.log("CarrierInteractor", e) }
  }

  fun getFinishedPayment(uri: Uri, packageName: String): Single<CarrierPaymentModel> {
    return getAddresses(packageName)
        .flatMapObservable { addresses ->
          observeTransactionUpdates(getUidFromUri(uri)!!, addresses.address,
              addresses.signedAddress)
        }
        .firstOrError()
        .map { paymentModel ->
          if (!paymentModel.networkError.hasError) {
            return@map paymentModel.copy(
                networkError = Error(hasError = true, isNetworkError = false, code = -1,
                    message = getErrorReasonFromUri(uri)))
          }
          return@map paymentModel
        }
  }

  fun cancelTransaction(uid: String, packageName: String): Completable {
    return getAddresses(packageName)
        .flatMapCompletable { addresses ->
          repository.cancelPayment(uid, addresses.address, addresses.signedAddress)
              .ignoreElement()
        }
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

  fun getTransactionBuilder(transactionData: String): Single<TransactionBuilder> {
    return inAppPurchaseInteractor.parseTransaction(transactionData, true)
        .subscribeOn(ioScheduler)
  }

  private fun getAddresses(packageName: String): Single<WalletAddresses> {
    return Single.zip(walletService.getAndSignCurrentWalletAddress()
        .subscribeOn(ioScheduler), partnerAddressService.getStoreAddressForPackage(packageName)
        .subscribeOn(ioScheduler),
        partnerAddressService.getOemAddressForPackage(packageName)
            .subscribeOn(ioScheduler), Function3 { addressModel, storeAddress, oemAddress ->
      return@Function3 WalletAddresses(addressModel.address, addressModel.signedAddress,
          storeAddress, oemAddress)
    })
  }

  private fun isInApp(type: String): Boolean {
    return type.equals("INAPP", ignoreCase = true)
  }

  private fun observeTransactionUpdates(uid: String, walletAddress: String,
                                        walletSignature: String): Observable<CarrierPaymentModel> {
    return Observable.interval(0, 5, TimeUnit.SECONDS, ioScheduler)
        .timeInterval()
        .switchMap {
          repository.getPayment(uid, walletAddress, walletSignature)
        }
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
    return Single.zip(walletBlockedInteract.isWalletBlocked()
        .subscribeOn(ioScheduler), isWalletVerified().subscribeOn(ioScheduler),
        BiFunction { blocked, verified ->
          WalletStatus(blocked, verified)
        })
  }

  private fun isWalletVerified(): Single<Boolean> =
      walletService.getWalletAddress()
          .flatMap { smsValidationInteract.isValidated(it) }
          .onErrorReturn { true }

}