package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.http.*

class AdyenPaymentRepository(private val adyenApi: AdyenApi,
                             private val adyenResponseMapper: AdyenResponseMapper) {

  fun loadPaymentInfo(methods: Methods, value: String,
                      currency: String, walletAddress: String): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo(walletAddress, value, currency, methods.transactionType)
        .map { adyenResponseMapper.map(it, methods) }
        .onErrorReturn { adyenResponseMapper.mapInfoModelError(it) }
  }

  fun makePayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean, hasCvc: Boolean,
                  supportedShopperInteractions: List<String>, returnUrl: String, value: String,
                  currency: String, reference: String?, paymentType: String, walletAddress: String,
                  origin: String?, packageName: String?, metadata: String?, sku: String?,
                  callbackUrl: String?, transactionType: String, developerWallet: String?,
                  storeWallet: String?, oemWallet: String?, userWallet: String?,
                  walletSignature: String,
                  billingAddress: AdyenBillingAddress?): Single<PaymentModel> {
    val shopperInteraction = if (!hasCvc && supportedShopperInteractions.contains("ContAuth")) {
      "ContAuth"
    } else "Ecommerce"
    return adyenApi.makePayment(walletAddress, walletSignature,
        Payment(adyenPaymentMethod, shouldStoreMethod, returnUrl, shopperInteraction,
            billingAddress, callbackUrl, packageName, metadata, paymentType, origin, sku, reference,
            transactionType, currency, value, developerWallet, storeWallet, oemWallet, userWallet))
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn { adyenResponseMapper.mapPaymentModelError(it) }
  }

  fun submitRedirect(uid: String, walletAddress: String, walletSignature: String,
                     details: JSONObject, paymentData: String?): Single<PaymentModel> {
    val json = convertToJson(details)
    return adyenApi.submitRedirect(uid, walletAddress, walletSignature,
        AdyenPayment(json, paymentData))
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn { adyenResponseMapper.mapPaymentModelError(it) }
  }

  fun disablePayments(walletAddress: String): Single<Boolean> {
    return adyenApi.disablePayments(DisableWallet(walletAddress))
        .toSingleDefault(true)
        .doOnError { it.printStackTrace() }
        .onErrorReturn { false }
  }

  fun getTransaction(uid: String, walletAddress: String,
                     signedWalletAddress: String): Single<PaymentModel> {
    return adyenApi.getTransaction(uid, walletAddress, signedWalletAddress)
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn { adyenResponseMapper.mapPaymentModelError(it) }
  }

  //This method is used to avoid the nameValuePairs key problem that occurs when we pass a JSONObject trough a GSON converter
  private fun convertToJson(details: JSONObject): JsonObject {
    val json = JsonObject()
    val keys = details.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      val value = details.get(key)
      if (value is String) json.addProperty(key, value)
    }
    return json
  }

  interface AdyenApi {

    @GET("payment-methods")
    fun loadPaymentInfo(@Query("wallet.address") walletAddress: String,
                        @Query("price.value") value: String,
                        @Query("price.currency") currency: String,
                        @Query("method") methods: String
    ): Single<PaymentMethodsResponse>


    @GET("transactions/{uid}")
    fun getTransaction(@Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
                       @Query("wallet.signature")
                       walletSignature: String): Single<TransactionResponse>

    @POST("transactions")
    fun makePayment(@Query("wallet.address") walletAddress: String,
                    @Query("wallet.signature") walletSignature: String,
                    @Body payment: Payment): Single<AdyenTransactionResponse>

    @PATCH("transactions/{uid}")
    fun submitRedirect(@Path("uid") uid: String,
                       @Query("wallet.address") address: String,
                       @Query("wallet.signature") signature: String,
                       @Body payment: AdyenPayment): Single<AdyenTransactionResponse>

    @POST("disable-recurring")
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
                     @SerializedName("wallets.store") val store: String?,
                     @SerializedName("wallets.oem") val oem: String?,
                     @SerializedName("wallets.user") val user: String?)

  data class AdyenPayment(@SerializedName("payment.details") val details: Any,
                          @SerializedName("payment.data") val data: String?)

  data class DisableWallet(@SerializedName("wallet.address") val walletAddress: String)

  enum class Methods(val adyenType: String, val transactionType: String) {
    CREDIT_CARD("scheme", "credit_card"), PAYPAL("paypal", "paypal")
  }
}