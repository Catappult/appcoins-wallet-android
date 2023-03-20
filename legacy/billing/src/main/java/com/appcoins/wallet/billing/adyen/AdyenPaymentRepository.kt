package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.core.network.microservices.api.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.api.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.*
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

class AdyenPaymentRepository @Inject constructor(
  private val adyenApi: BrokerVerificationApi.AdyenApi,
  private val brokerBdsApi: BrokerVerificationApi.BrokerBdsApi,
  private val subscriptionsApi: SubscriptionBillingApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val logger: Logger
) {

  fun loadPaymentInfo(methods: Methods, value: String,
                      currency: String, walletAddress: String): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo(walletAddress, value, currency, methods.transactionType)
        .map { adyenResponseMapper.map(it, methods) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          adyenResponseMapper.mapInfoModelError(it)
        }
  }

  fun makePayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
                  supportedShopperInteractions: List<String>, returnUrl: String, value: String,
                  currency: String, reference: String?, paymentType: String, walletAddress: String,
                  origin: String?, packageName: String?, metadata: String?, sku: String?,
                  callbackUrl: String?, transactionType: String, developerWallet: String?,
                  entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
                  userWallet: String?,
                  walletSignature: String,
                  billingAddress: AdyenBillingAddress?,
                  referrerUrl: String?): Single<PaymentModel> {
    val shopperInteraction = if (!hasCvc && supportedShopperInteractions.contains("ContAuth")) {
      "ContAuth"
    } else "Ecommerce"
    return if (transactionType == BillingSupportedType.INAPP_SUBSCRIPTION.name) {
      subscriptionsApi.getSkuSubscriptionToken(packageName!!, sku!!, currency, walletAddress,
          walletSignature)
          .map {
            TokenPayment(adyenPaymentMethod, shouldStoreMethod, returnUrl, shopperInteraction,
                billingAddress, callbackUrl, metadata, paymentType, origin, reference,
                developerWallet, entityOemId, entityDomain, entityPromoCode, userWallet,
                referrerUrl, it)
          }
          .flatMap { adyenApi.makeTokenPayment(walletAddress, walletSignature, it) }
          .map { adyenResponseMapper.map(it) }
          .onErrorReturn {
            logger.log("AdyenPaymentRepository", it)
            adyenResponseMapper.mapPaymentModelError(it)
          }
    } else {
      return adyenApi.makeAdyenPayment(
        walletAddress, walletSignature,
        PaymentDetails(
          adyenPaymentMethod, shouldStoreMethod, returnUrl, shopperInteraction,
          billingAddress, callbackUrl, packageName, metadata, paymentType, origin, sku,
          reference,
          transactionType, currency, value, developerWallet, entityOemId, entityDomain,
          entityPromoCode,
          userWallet,
          referrerUrl
        )
      )
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          adyenResponseMapper.mapPaymentModelError(it)
          }
    }
  }

  fun submitRedirect(uid: String, walletAddress: String, walletSignature: String,
                     details: JsonObject, paymentData: String?): Single<PaymentModel> {
    return adyenApi.submitRedirect(uid, walletAddress, walletSignature,
        AdyenPayment(details, paymentData))
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          adyenResponseMapper.mapPaymentModelError(it)
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

  fun getTransaction(uid: String, walletAddress: String,
                     signedWalletAddress: String): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(uid, walletAddress, signedWalletAddress)
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