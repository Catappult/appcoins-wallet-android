package com.appcoins.wallet.billing.skills

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.core.network.microservices.api.AdyenTransactionResponse
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.model.Method
import com.appcoins.wallet.core.network.microservices.model.Payment
import com.appcoins.wallet.core.network.microservices.model.PaymentRequest
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject


class SkillsPaymentRepository @Inject constructor(private val adyenApi: AdyenApi,
                                                  private val adyenResponseMapper: AdyenResponseMapper) {


  fun makeSkillsPayment(returnUrl: String,
                        walletAddress: String,
                        walletSignature: String,
                        productToken: String, encryptedCardNumber: String?,
                        encryptedExpiryMonth: String?,
                        encryptedExpiryYear: String?,
                        encryptedSecurityCode: String): Single<PaymentModel> {
    return adyenApi.makePayment(walletAddress, walletSignature,
        PaymentRequest(Payment(
            Method("scheme", encryptedCardNumber, encryptedExpiryMonth, encryptedExpiryYear,
                encryptedSecurityCode), returnUrl), productToken))
        .map { adyenResponseMapper.map(it) }
        .onErrorReturn { adyenResponseMapper.mapPaymentModelError(it) }
  }


  interface AdyenApi {


    @POST("8.20200815/gateways/adyen_v2/transactions")
    @Headers("Content-Type: application/json;format=product_token")
    fun makePayment(@Query("wallet.address") walletAddress: String,
                    @Query("wallet.signature") walletSignature: String,
                    @Body payment: PaymentRequest): Single<AdyenTransactionResponse>
  }
}