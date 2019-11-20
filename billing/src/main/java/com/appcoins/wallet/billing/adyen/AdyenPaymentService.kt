package com.appcoins.wallet.billing.adyen

import android.util.Log
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

class AdyenPaymentService(private val adyenApi: AdyenApi) {

  fun loadPaymentInfo(methods: Methods, value: String,
                      currency: String): Single<PaymentInfoModel> {
    return adyenApi.loadPaymentInfo("2", currency)
        .map { map(it, methods) }
        .onErrorReturn { map(it) }
  }

  private fun map(throwable: Throwable): PaymentInfoModel {
    Log.d("TAG123", "HERE" + throwable.cause)
    return PaymentInfoModel(null, Error(true, throwable.isNoNetworkException()))
  }

  fun makePayment(value: String, reference: String, paymentMethod: PaymentMethod,
                  returnUrl: String): Single<PaymentModel> {
    return adyenApi.makePayment(value, reference, paymentMethod, returnUrl)
        .map { PaymentModel(it.resultCode, it.pspReference, it.action.type, it.action.url) }
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
    fun makePayment(@Query("amount") value: String,
                    @Query("reference") reference: String,
                    @Query("paymentMethod") paymentMethod: PaymentMethod,
                    @Query("returnUrl") returnUrl: String): Single<MakePaymentResponse>
  }

  enum class Methods(val id: String) {
    CREDIT_CARD("Credit Card"), PAYPAL("PayPal")
  }

  fun Throwable?.isNoNetworkException(): Boolean {
    return this != null && (this is IOException || this.cause != null && this.cause is IOException)
  }
}