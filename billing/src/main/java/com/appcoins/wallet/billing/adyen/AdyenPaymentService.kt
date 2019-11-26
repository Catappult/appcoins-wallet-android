package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.util.Error
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import java.io.IOException

class AdyenPaymentService(private val adyenApi: AdyenApi) {

  fun loadPaymentInfo(methods: Methods, value: String,
                      currency: String, walletAddress: String): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo(value, currency, walletAddress)
        .map { map(it, methods) }
        .onErrorReturn { mapInfoModelError(it) }
  }

  fun makePayment(value: String, currency: String, reference: String?, encryptedCardNumber: String?,
                  encryptedExpiryMonth: String?, encryptedExpiryYear: String?,
                  encryptedSecurityCode: String?, paymentId: String?, type: String,
                  walletAddress: String?, returnUrl: String?,
                  savedPaymentMethod: Boolean = false): Single<PaymentModel> {
    return adyenApi.makePayment(value, currency, encryptedCardNumber,
        encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, null, reference,
        paymentId, type, walletAddress, returnUrl, savedPaymentMethod)
        .map { map(it) }
        .onErrorReturn { mapModelError(it) }
  }

  fun submitRedirect(payload: String?, paymentData: String?): Single<PaymentModel> {
    return adyenApi.submitRedirect(payload, paymentData)
        .map { map(it) }
        .onErrorReturn { mapModelError(it) }
  }

  fun disablePayments(walletAddress: String): Single<Boolean> {
    return adyenApi.disablePayments(walletAddress)
        .toSingleDefault(true)
        .onErrorReturn { false }
  }

  private fun map(response: PaymentMethodsApiResponse,
                  method: Methods): PaymentInfoModel {
    val storedPaymentModel = findPaymentMethod(response.storedPaymentMethods, method, true)
    return if (storedPaymentModel.error.hasError) {
      findPaymentMethod(response.paymentMethods, method, false)
    } else {
      storedPaymentModel
    }
  }

  private fun map(response: MakePaymentResponse): PaymentModel {
    return PaymentModel(response.resultCode, response.refusalReason,
        response.refusalReasonCode?.toInt(),
        response.action, response.action?.url, response.action?.paymentData)
  }

  private fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    return PaymentInfoModel(null, false, Error(true, throwable.isNoNetworkException()))
  }

  private fun mapModelError(throwable: Throwable): PaymentModel {
    return PaymentModel(Error(true, throwable.isNoNetworkException()))
  }

  private fun findPaymentMethod(paymentMethods: List<PaymentMethod>?,
                                method: Methods, isStored: Boolean): PaymentInfoModel {
    paymentMethods?.let {
      for (paymentMethod in it) {
        if (paymentMethod.type == method.type) return PaymentInfoModel(paymentMethod, isStored)
      }
    }
    return PaymentInfoModel(null, false, Error(true))
  }

  interface AdyenApi {

    @GET("adyen/methods")
    fun loadPaymentInfo(@Query("value") value: String,
                        @Query("currency") currency: String,
                        @Query("wallet.address") walletAddress: String
    ): Single<PaymentMethodsApiResponse>

    @POST("adyen/payment")
    fun makePayment(@Query("value") value: String,
                    @Query("currency") currency: String,
                    @Query("encrypted_card_number") encryptedCardNumber: String?,
                    @Query("encrypted_expiry_month") encryptedExpiryMonth: String?,
                    @Query("encrypted_expiry_year") encryptedExpiryYear: String?,
                    @Query("encrypted_security_code") encryptedSecurityCode: String?,
                    @Query("holder_name") holderName: String?,
                    @Query("reference") reference: String?,
                    @Query("token") token: String?,
                    @Query("type") paymentMethod: String,
                    @Query("wallet.address") walletAddress: String?,
                    @Query("redirect_url") returnUrl: String?,
                    @Query("store_details") savePayment: Boolean): Single<MakePaymentResponse>

    @FormUrlEncoded
    @POST("adyen/payment/details")
    fun submitRedirect(@Field("payload") payload: String?,
                       @Field("payment_data") paymentData: String?): Single<MakePaymentResponse>

    @POST("adyen/payment/disable")
    fun disablePayments(@Query("wallet.address") walletAddress: String): Completable
  }


  enum class Methods(val type: String) {
    CREDIT_CARD("scheme"), PAYPAL("paypal")
  }

  fun Throwable?.isNoNetworkException(): Boolean {
    return this != null && (this is IOException || this.cause != null && this.cause is IOException)
  }
}