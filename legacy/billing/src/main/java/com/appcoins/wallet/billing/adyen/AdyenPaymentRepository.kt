package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.AdyenPayment
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.CreditCardCVCResponse
import com.appcoins.wallet.core.network.microservices.model.DisableWallet
import com.appcoins.wallet.core.network.microservices.model.PaymentDetails
import com.appcoins.wallet.core.network.microservices.model.TokenPayment
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.sharedpreferences.CardPaymentDataSource
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

class AdyenPaymentRepository @Inject constructor(
  private val adyenApi: AdyenApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val subscriptionsApi: SubscriptionBillingApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val cardPaymentDataSource: CardPaymentDataSource,
  private val rxSchedulers: RxSchedulers,
  private val logger: Logger
) {

  fun loadPaymentInfo(
    methods: Methods,
    value: String,
    currency: String,
    walletAddress: String,
  ): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo(
      walletAddress = walletAddress,
      value = value,
      currency = currency,
      methods = methods.transactionType
    )
      .map { adyenResponseMapper.map(it, methods) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapInfoModelError(it)
      }
  }

  fun getStoredCards(
    methods: Methods,
    value: String,
    currency: String?,
    walletAddress: String,
  ): Single<List<StoredPaymentMethod>> {
    return adyenApi.loadPaymentInfo(
      walletAddress = walletAddress,
      value = value,
      currency = currency ?: "USD",
      methods = methods.transactionType
    )
      .map { adyenResponseMapper.mapToStoredCards(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        listOf()
      }
  }

  fun makePayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    hasCvc: Boolean,
    supportedShopperInteractions: List<String>,
    returnUrl: String,
    value: String,
    currency: String,
    reference: String?,
    paymentType: String,
    walletAddress: String,
    origin: String?,
    packageName: String?,
    metadata: String?,
    sku: String?,
    callbackUrl: String?,
    transactionType: String,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
    guestWalletId: String?,
    externalBuyerReference: String?,
    isFreeTrial: Boolean?
  ): Single<PaymentModel> {
    val shopperInteraction = if (!hasCvc && supportedShopperInteractions.contains("ContAuth")) {
      "ContAuth"
    } else
      "Ecommerce"
    return if (transactionType == BillingSupportedType.INAPP_SUBSCRIPTION.name) {
      if (isFreeTrial == true) {
        subscriptionsApi.getSkuSubscriptionFreeTrialToken(
          domain = packageName!!,
          sku = sku!!,
          currency = currency,
          walletAddress = walletAddress,
          externalBuyerReference = externalBuyerReference,
          isFreeTrial = isFreeTrial
        )
      } else {
        subscriptionsApi.getSkuSubscriptionToken(
          domain = packageName!!,
          sku = sku!!,
          currency = currency,
          walletAddress = walletAddress,
        )
      }
        .subscribeOn(rxSchedulers.io)
        .map {
          TokenPayment(
            adyenPaymentMethod = adyenPaymentMethod,
            shouldStoreMethod = shouldStoreMethod,
            returnUrl = returnUrl,
            shopperInteraction = shopperInteraction,
            callbackUrl = callbackUrl,
            metadata = metadata,
            method = paymentType,
            origin = origin,
            reference = reference,
            entityOemId = entityOemId,
            entityDomain = entityDomain,
            entityPromoCode = entityPromoCode,
            user = userWallet,
            referrerUrl = referrerUrl,
            guestWalletId = guestWalletId,
            token = it
          )
        }
        .flatMap {
          adyenApi.makeTokenPayment(
            walletAddress = walletAddress,
            payment = it
          )
        }
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          adyenResponseMapper.mapPaymentModelError(it)
        }
    } else {
      return adyenApi.makeAdyenPayment(
        walletAddress = walletAddress,
        payment = PaymentDetails(
          adyenPaymentMethod = adyenPaymentMethod,
          shouldStoreMethod = shouldStoreMethod,
          returnUrl = returnUrl,
          shopperInteraction = shopperInteraction,
          callbackUrl = callbackUrl,
          domain = packageName,
          metadata = metadata,
          method = paymentType,
          origin = origin,
          sku = sku,
          reference = reference,
          type = transactionType,
          currency = currency,
          value = value,
          entityOemId = entityOemId,
          entityDomain = entityDomain,
          entityPromoCode = entityPromoCode,
          user = userWallet,
          referrerUrl = referrerUrl,
          guestWalletId = guestWalletId,
        )
      )
        .subscribeOn(rxSchedulers.io)
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          adyenResponseMapper.mapPaymentModelError(it)
        }
    }
  }

  fun submitRedirect(
    uid: String,
    walletAddress: String,
    details: JsonObject,
    paymentData: String?
  ): Single<PaymentModel> {
    return adyenApi.submitRedirect(
      uid = uid,
      address = walletAddress,
      payment = AdyenPayment(details, paymentData)
    )
      .subscribeOn(rxSchedulers.io)
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  fun disablePayments(walletAddress: String): Single<Boolean> {
    return adyenApi.disablePayments(DisableWallet(walletAddress, null))
      .toSingleDefault(true)
      .doOnError { it.printStackTrace() }
      .onErrorReturn { false }
  }

  fun removeSavedCard(walletAddress: String, recurringReference: String?): Single<Boolean> {
    return adyenApi.disablePayments(DisableWallet(walletAddress, recurringReference))
      .toSingleDefault(true)
      .doOnError { it.printStackTrace() }
      .onErrorReturn { false }
  }

  fun getTransaction(
    uid: String,
    walletAddress: String,
    signedWalletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(
      uId = uid,
      walletAddress = walletAddress,
      walletSignature = signedWalletAddress
    )
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapPaymentModelError(it)
      }
  }

  fun getCreditCardNeedCVC(): Single<CreditCardCVCResponse> {
    return adyenApi.getCreditCardNeedCVC().map {
      if (!cardPaymentDataSource.isMandatoryCvc()) it
      else CreditCardCVCResponse(needAskCvc = true)
    }
      .doOnError { it.printStackTrace() }
      .onErrorReturn { CreditCardCVCResponse(needAskCvc = true) }
  }

  fun setMandatoryCVC(mandatoryCvc: Boolean) {
    cardPaymentDataSource.setMandatoryCvc(mandatoryCvc)
  }

  enum class Methods(val adyenType: String, val transactionType: String) {
    CREDIT_CARD("scheme", "credit_card"),
    PAYPAL("paypal", "paypal")
  }
}