package com.asfoundation.wallet.manage_cards.usecases

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Single
import javax.inject.Inject

class DeleteStoredCardUseCase @Inject constructor(
  private val adyenPaymentRepository: AdyenPaymentRepository,
  private val walletService: WalletService,
  private val rxSchedulers: RxSchedulers,

  ) {

  operator fun invoke(recurringReference: String): Single<Boolean> {
    return walletService.getWalletAddress().subscribeOn(rxSchedulers.io).flatMap { wallet ->
      adyenPaymentRepository.removeSavedCard(wallet, recurringReference)
    }
  }

}