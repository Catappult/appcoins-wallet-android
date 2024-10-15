package com.asfoundation.wallet.topup.usecases

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.broker.AdyenApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentInfoFilterByCardModelUseCase @Inject constructor(
  private val adyenApi: AdyenApi,
  private val adyenResponseMapper: AdyenResponseMapper,
  private val walletService: WalletService,
  private val rxSchedulers: RxSchedulers,
  private val ewtObtainer: EwtAuthenticatorService,
) {

  operator fun invoke(
    value: String,
    currency: String,
    cardId: String
  ): Single<PaymentInfoModel> {
    return Single.zip(
      walletService.getWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
    ) { walletModel, ewt ->
      Pair(walletModel, ewt)
    }
      .flatMap { pair ->
        val wallet = pair.first
        val ewt = pair.second
        adyenApi.loadPaymentInfo(
          wallet,
          ewt,
          value,
          currency,
          mapPaymentToService().transactionType,
        )
          .map { adyenResponseMapper.mapWithFilterByCard(it, mapPaymentToService(), cardId) }
          .onErrorReturn {
            adyenResponseMapper.mapInfoModelError(it)
          }
      }
  }

  private fun mapPaymentToService(): AdyenPaymentRepository.Methods =
    AdyenPaymentRepository.Methods.CREDIT_CARD
}

