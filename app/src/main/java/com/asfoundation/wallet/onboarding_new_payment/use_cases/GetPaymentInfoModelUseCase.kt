package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
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
) {

  operator fun invoke(
    paymentType: String, value: String, currency: String
  ): Single<PaymentInfoModel> =
    walletService.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMap { wallet ->
        adyenApi.loadPaymentInfo(
          walletAddress = wallet,
          value = value,
          currency = currency,
          methods = mapPaymentToService(paymentType).transactionType,
        )
          .map { adyenResponseMapper.map(it, mapPaymentToService(paymentType)) }
          .onErrorReturn { adyenResponseMapper.mapInfoModelError(it) }
      }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods =
    when (paymentType) {
      PaymentType.CARD.name -> AdyenPaymentRepository.Methods.CREDIT_CARD

      else -> AdyenPaymentRepository.Methods.PAYPAL
    }
}
