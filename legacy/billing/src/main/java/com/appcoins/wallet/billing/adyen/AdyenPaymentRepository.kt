package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.*
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

class AdyenPaymentRepository @Inject constructor(
  private val adyenApi: AdyenApi,
  private val brokerBdsApi: BrokerBdsApi,
  private val subscriptionsApi: SubscriptionBillingApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,
  private val logger: Logger
) {

  fun loadPaymentInfo(
    methods: Methods,
    value: String,
    currency: String,
    walletAddress: String,
    walletSignature: String,
    ewt: String
  ): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo(
      walletAddress,
      walletSignature,
      ewt,
      value,
      currency,
      methods.transactionType
    )
      .map {
        adyenResponseMapper.map(it, methods)
      }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        adyenResponseMapper.mapInfoModelError(it)
      }
  }

  fun makePayment(
    adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
    supportedShopperInteractions: List<String>, returnUrl: String, value: String,
    currency: String, reference: String?, paymentType: String, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String, developerWallet: String?,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?,
    walletSignature: String,
    billingAddress: AdyenBillingAddress?,
    referrerUrl: String?
  ): Single<PaymentModel> {
    val shopperInteraction = if (!hasCvc && supportedShopperInteractions.contains("ContAuth")) {
      "ContAuth"
    } else "Ecommerce"
    return if (transactionType == BillingSupportedType.INAPP_SUBSCRIPTION.name) {
      ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
        .flatMap { ewt ->  // TODO keep both for this one
          subscriptionsApi.getSkuSubscriptionToken(
            domain = packageName!!, sku = sku!!, currency = currency, walletAddress = walletAddress,
            walletSignature = walletSignature
          )
            .map {
              TokenPayment(
                adyenPaymentMethod, shouldStoreMethod, returnUrl, shopperInteraction,
                billingAddress, callbackUrl, metadata, paymentType, origin, reference,
                developerWallet, entityOemId, entityDomain, entityPromoCode, userWallet,
                referrerUrl, it
              )
            }
            .flatMap {
              adyenApi.makeTokenPayment(
                walletAddress = walletAddress,
                walletSignature = walletSignature,
                authorization = ewt,
                payment = it
              )
            }
            .map { adyenResponseMapper.map(it) }
            .onErrorReturn {
              logger.log("AdyenPaymentRepository", it)
              adyenResponseMapper.mapPaymentModelError(it)
            }
        }
    } else {
      return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
        .flatMap { ewt ->
          adyenApi.makeAdyenPayment(
            walletAddress, walletSignature, ewt,
            PaymentDetails(
              adyenPaymentMethod = adyenPaymentMethod,
              shouldStoreMethod = shouldStoreMethod,
              returnUrl = returnUrl,
              shopperInteraction = shopperInteraction,
              billingAddress = billingAddress,
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
              developer = developerWallet,
              entityOemId = entityOemId,
              entityDomain = entityDomain,
              entityPromoCode = entityPromoCode,
              user = userWallet,
              referrerUrl = referrerUrl
            )
          )
            .map { adyenResponseMapper.map(it) }
            .onErrorReturn {
              logger.log("AdyenPaymentRepository", it)
              adyenResponseMapper.mapPaymentModelError(it)
            }
        }
    }
  }

  fun submitRedirect(
    uid: String, walletAddress: String, walletSignature: String,
    details: JsonObject, paymentData: String?
  ): Single<PaymentModel> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        adyenApi.submitRedirect(
          uid = uid, address = walletAddress, signature = walletSignature, authorization = ewt,
          payment = AdyenPayment(details, paymentData)
        )
          .map { adyenResponseMapper.map(it) }
          .onErrorReturn {
            logger.log("AdyenPaymentRepository", it)
            adyenResponseMapper.mapPaymentModelError(it)
          }
      }
  }

  fun disablePayments(walletAddress: String): Single<Boolean> {
    return adyenApi.disablePayments(DisableWallet(walletAddress))
      .toSingleDefault(true)
      .doOnError { it.printStackTrace() }
      .onErrorReturn {
        false
      }
  }

  fun getTransaction(
    uid: String, walletAddress: String,
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

  enum class Methods(val adyenType: String, val transactionType: String) {
    CREDIT_CARD("scheme", "credit_card"), PAYPAL("paypal", "paypal")
  }
}