package com.asfoundation.wallet.manage_cards.usecases

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentInfoNewCardModelUseCase @Inject constructor(
  private val adyenApi: AdyenApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val walletService: WalletService,
  private val rxSchedulers: RxSchedulers,
) {

  operator fun invoke(
    value: String,
    currency: String
  ): Single<PaymentInfoModel> =
    walletService.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMap { wallet ->
        adyenApi.loadPaymentInfo(
          walletAddress = wallet,
          value = value,
          currency = currency,
          methods = mapPaymentToService().transactionType,
        )
          .map { adyenResponseMapper.mapWithoutStoredCard(it, mapPaymentToService()) }
          .onErrorReturn {
            adyenResponseMapper.mapInfoModelError(it)
          }
      }

  private fun mapPaymentToService(): AdyenPaymentRepository.Methods =
    AdyenPaymentRepository.Methods.CREDIT_CARD
}
