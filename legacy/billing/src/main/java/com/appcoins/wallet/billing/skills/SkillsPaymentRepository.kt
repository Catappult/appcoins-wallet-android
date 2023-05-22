package com.appcoins.wallet.billing.skills

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.network.microservices.model.Method
import com.appcoins.wallet.core.network.microservices.model.Payment
import com.appcoins.wallet.core.network.microservices.model.PaymentRequest
import io.reactivex.Single
import javax.inject.Inject


class SkillsPaymentRepository @Inject constructor(
  private val adyenApi: AdyenApi,
  private val adyenResponseMapper: AdyenResponseMapper
) {

  fun makeSkillsPayment(
    returnUrl: String,
    walletAddress: String,
    walletSignature: String,
    productToken: String, encryptedCardNumber: String?,
    encryptedExpiryMonth: String?,
    encryptedExpiryYear: String?,
    encryptedSecurityCode: String
  ): Single<PaymentModel> {
    return adyenApi.makeAdyenBodyPayment(
      walletAddress, walletSignature,
      PaymentRequest(
        Payment(
          Method(
            "scheme", encryptedCardNumber, encryptedExpiryMonth, encryptedExpiryYear,
            encryptedSecurityCode
          ), returnUrl
        ), productToken
      )
    )
      .map { adyenResponseMapper.map(it) }
      .onErrorReturn { adyenResponseMapper.mapPaymentModelError(it) }
  }
}