package com.asfoundation.wallet.manage_cards.usecases

import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencyUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetStoredCardsUseCase @Inject constructor(
  private val adyenPaymentRepository: AdyenPaymentRepository,
  private val walletService: WalletService,
  private val getCachedCurrencyUseCase: GetCachedCurrencyUseCase,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,

  ) {

  operator fun invoke(): Single<List<StoredPaymentMethod>> {
    return Single.zip(
      walletService.getWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
    ) { walletModel, ewt ->
      Pair(walletModel, ewt)
    }
      .flatMap { pair ->
        val wallet = pair.first
        val ewt = pair.second
        val currency = getCachedCurrencyUseCase()
        adyenPaymentRepository
          .getStoredCards(
            AdyenPaymentRepository.Methods.CREDIT_CARD,
            "10",
            currency, // this shouldn't be dependent on the currency, the user should be able to see all cards listed. This limitation arises because we have 2 separate merchants
            wallet,
            ewt
          )
      }
  }

}