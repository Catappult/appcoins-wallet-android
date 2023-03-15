package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentInfoModelUseCase @Inject constructor(
  private val adyenApi: AdyenPaymentRepository.AdyenApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val walletService: WalletService,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(
    paymentType: String, value: String,
    currency: String
  ): Single<PaymentInfoModel> {
    return walletService.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMap { walletAddress ->
        adyenApi.loadPaymentInfo(
          walletAddress,
          value,
          currency,
          mapPaymentToService(paymentType).transactionType
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

