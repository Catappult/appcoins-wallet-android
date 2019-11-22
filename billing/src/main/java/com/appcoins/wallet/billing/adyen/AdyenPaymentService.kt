package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

class AdyenPaymentService(private val adyenApi: AdyenApi) {

  fun loadPaymentInfo(methods: Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo(value, currency)
        .map { map(it, methods) }
        .onErrorReturn { map(it) }
  }

  private fun map(throwable: Throwable): PaymentInfoModel {
    return PaymentInfoModel(null, Error(true, throwable.isNoNetworkException()))
  }

  fun makePayment(value: String, currency: String, reference: String, encryptedCardNumber: String?,
                  encryptedExpiryMonth: String?, encryptedExpiryYear: String?,
                  encryptedSecurityCode: String?, type: String,
                  returnUrl: String?): Single<PaymentModel> {
    return adyenApi.makePayment(value, currency, encryptedCardNumber,
        encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, null, reference,
        type, returnUrl)
        .map {
          PaymentModel(it.resultCode, it.refusalReason, it.refusalReasonCode?.toInt(),
              it.action?.type, it.action?.url)
        }
        .onErrorReturn { PaymentModel() }
  }

  private fun map(response: PaymentMethodsApiResponse,
                  method: Methods): PaymentInfoModel {
    val paymentMethods = response.paymentMethods
    paymentMethods?.let {
      for (paymentMethod in it) {
        if (paymentMethod.name == method.id) return PaymentInfoModel(paymentMethod)
      }
    }
    return PaymentInfoModel(null, Error(true))
  }

  interface AdyenApi {

    @GET("adyen/methods")
    fun loadPaymentInfo(@Query("value") value: String,
                        @Query("currency") currency: String): Single<PaymentMethodsApiResponse>

    @POST("adyen/payment")
    fun makePayment(@Query("value") value: String,
                    @Query("currency") currency: String,
                    @Query("encrypted_card_number") encryptedCardNumber: String?,
                    @Query("encrypted_expiry_month") encryptedExpiryMonth: String?,
                    @Query("encrypted_expiry_year") encryptedExpiryYear: String?,
                    @Query("encrypted_security_code") encryptedSecurityCode: String?,
                    @Query("holder_name") holderName: String?,
                    @Query("reference") reference: String,
                    @Query("type") paymentMethod: String,
                    @Query("return_url") returnUrl: String?): Single<MakePaymentResponse>
  }

  enum class Methods(val id: String) {
    CREDIT_CARD("Credit Card"), PAYPAL("PayPal")
  }

  fun Throwable?.isNoNetworkException(): Boolean {
    return this != null && (this is IOException || this.cause != null && this.cause is IOException)
  }
}