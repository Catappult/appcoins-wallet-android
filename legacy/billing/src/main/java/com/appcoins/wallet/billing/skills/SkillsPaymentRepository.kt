package com.appcoins.wallet.billing.skills

import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.network.microservices.model.Method
import com.appcoins.wallet.core.network.microservices.model.Payment
import com.appcoins.wallet.core.network.microservices.model.PaymentRequest
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject


class SkillsPaymentRepository @Inject constructor(
  private val adyenApi: AdyenApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers
) {

  fun makeSkillsPayment(
    returnUrl: String,
    walletAddress: String,
    productToken: String, encryptedCardNumber: String?,
    encryptedExpiryMonth: String?,
    encryptedExpiryYear: String?,
    encryptedSecurityCode: String
  ): Single<PaymentModel> {
    return ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        adyenApi.makeAdyenBodyPayment(
          walletAddress = walletAddress, authorization = ewt,
          payment = PaymentRequest(
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
}