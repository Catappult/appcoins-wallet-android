package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentService
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class AdyenPaymentInteractor(
    private val adyenPaymentService: AdyenPaymentService,
    private val inAppPurchaseInteractor: InAppPurchaseInteractor,
    private val billingMessagesMapper: BillingMessagesMapper,
    private val findDefaultWalletInteract: FindDefaultWalletInteract,
    private val partnerAddressService: AddressService
) {

  fun loadPaymentInfo(methods: AdyenPaymentService.Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return findDefaultWalletInteract.find()
        .flatMap { adyenPaymentService.loadPaymentInfo(methods, value, currency, it.address) }
  }

  fun makePayment(adyenPaymentMethod: String, value: String, currency: String, reference: String,
                  paymentType: String, returnUrl: String, origin: String?, packageName: String,
                  metadata: String?, sku: String?, callbackUrl: String?,
                  transactionType: String, developerWallet: String,
                  userWallet: String): Single<PaymentModel> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          Single.zip(
              partnerAddressService.getStoreAddressForPackage(packageName),
              partnerAddressService.getOemAddressForPackage(packageName),
              BiFunction { storeAddress: String, oemAddress: String ->
                Pair(storeAddress, oemAddress)
              })
              .flatMap {
                adyenPaymentService.makePayment(adyenPaymentMethod, value, currency, reference,
                    paymentType, wallet.address, returnUrl, origin, packageName, metadata, sku,
                    callbackUrl, transactionType, developerWallet, it.first, it.second, userWallet)
              }
        }
  }

  fun submitRedirect(payload: String, paymentData: String?): Single<PaymentModel> {
    return adyenPaymentService.submitRedirect(payload, paymentData)
  }

  fun disablePayments(): Single<Boolean> {
    return findDefaultWalletInteract.find()
        .flatMap { adyenPaymentService.disablePayments(it.address) }
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
                                scheduler: Scheduler): Single<Bundle> { //TODO
    return Single.just(Bundle())
  }
}
