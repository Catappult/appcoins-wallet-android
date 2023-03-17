package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.core.network.microservices.model.TransactionResponse
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.core.network.microservices.api.AdyenTransactionResponse
import com.appcoins.wallet.core.network.microservices.api.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.api.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import javax.inject.Inject

class AdyenPaymentRepository @Inject constructor(
  private val adyenApi: AdyenApi,
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
      return adyenApi.makePayment(walletAddress, walletSignature,
          Payment(adyenPaymentMethod, shouldStoreMethod, returnUrl, shopperInteraction,
              billingAddress, callbackUrl, packageName, metadata, paymentType, origin, sku,
              reference,
              transactionType, currency, value, developerWallet, entityOemId, entityDomain,
              entityPromoCode,
              userWallet,
              referrerUrl))
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

  interface AdyenApi {

    @GET("8.20200815/gateways/adyen_v2/payment-methods")
    fun loadPaymentInfo(@Query("wallet.address") walletAddress: String,
                        @Query("price.value") value: String,
                        @Query("price.currency") currency: String,
                        @Query("method") methods: String
    ): Single<PaymentMethodsResponse>


    @GET("8.20200815/gateways/adyen_v2/transactions/{uid}")
    fun getTransaction(@Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
                       @Query("wallet.signature")
                       walletSignature: String): Single<TransactionResponse>

    @POST("8.20200815/gateways/adyen_v2/transactions")
    fun makePayment(@Query("wallet.address") walletAddress: String,
                    @Query("wallet.signature") walletSignature: String,
                    @Body payment: Payment): Single<AdyenTransactionResponse>

    @Headers("Content-Type: application/json;format=product_token")
    @POST("8.20200815/gateways/adyen_v2/transactions")
    fun makeTokenPayment(@Query("wallet.address") walletAddress: String,
                         @Query("wallet.signature") walletSignature: String,
                         @Body payment: TokenPayment): Single<AdyenTransactionResponse>

    @PATCH("8.20200815/gateways/adyen_v2/transactions/{uid}")
    fun submitRedirect(@Path("uid") uid: String,
                       @Query("wallet.address") address: String,
                       @Query("wallet.signature") signature: String,
                       @Body payment: AdyenPayment): Single<AdyenTransactionResponse>

    @POST("8.20200815/gateways/adyen_v2/disable-recurring")
    fun disablePayments(@Body wallet: DisableWallet): Completable
  }

  data class Payment(@SerializedName("payment.method") val adyenPaymentMethod: ModelObject,
                     @SerializedName("payment.store_method") val shouldStoreMethod: Boolean,
                     @SerializedName("payment.return_url") val returnUrl: String,
                     @SerializedName("payment.shopper_interaction") val shopperInteraction: String?,
                     @SerializedName("payment.billing_address")
                     val billingAddress: AdyenBillingAddress?,
                     @SerializedName("callback_url") val callbackUrl: String?,
                     @SerializedName("domain") val domain: String?,
                     @SerializedName("metadata") val metadata: String?,
                     @SerializedName("method") val method: String?,
                     @SerializedName("origin") val origin: String?,
                     @SerializedName("product") val sku: String?,
                     @SerializedName("reference") val reference: String?,
                     @SerializedName("type") val type: String?,
                     @SerializedName("price.currency") val currency: String?,
                     @SerializedName("price.value") val value: String?,
                     @SerializedName("wallets.developer") val developer: String?,
                     @SerializedName("entity.oemid") val entityOemId: String?,
                     @SerializedName("entity.domain") val entityDomain: String?,
                     @SerializedName("entity.promo_code") val entityPromoCode: String?,
                     @SerializedName("wallets.user") val user: String?,
                     @SerializedName("referrer_url") val referrerUrl: String?)

  data class AdyenPayment(@SerializedName("payment.details") val details: Any,
                          @SerializedName("payment.data") val data: String?)

  data class DisableWallet(@SerializedName("wallet.address") val walletAddress: String)

  enum class Methods(val adyenType: String, val transactionType: String) {
    CREDIT_CARD("scheme", "credit_card"), PAYPAL("paypal", "paypal")
  }
}