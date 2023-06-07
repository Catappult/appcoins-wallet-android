package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentInfoModelUseCase @Inject constructor(
  private val adyenApi: AdyenApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val walletService: WalletService,
  private val rxSchedulers: RxSchedulers,
  private val ewtObtainer: EwtAuthenticatorService,
) {

  operator fun invoke(
    paymentType: String, value: String,
    currency: String
  ): Single<PaymentInfoModel> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
    ) { walletModel, ewt ->
      Pair(walletModel, ewt)
    }
      .flatMap { pair ->
        val walletModel = pair.first
        val ewt = pair.second
        adyenApi.loadPaymentInfo(
          walletModel.address,
          walletModel.signedAddress,
          ewt,
          value,
          currency,
          mapPaymentToService(paymentType).transactionType,
        )
          .map { adyenResponseMapper.map(it, mapPaymentToService(paymentType)) }
          .onErrorReturn {
            adyenResponseMapper.mapInfoModelError(it)
          }
      }
  }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods =
    if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    } else {
      AdyenPaymentRepository.Methods.PAYPAL
    }
}

