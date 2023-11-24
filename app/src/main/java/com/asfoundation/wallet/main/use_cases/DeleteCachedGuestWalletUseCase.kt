package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import com.asfoundation.wallet.repository.CachedGuestWalletRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class DeleteCachedGuestWalletUseCase @Inject constructor(
  private val cachedGuestWalletRepository: CachedGuestWalletRepository,
  private val getCurrentWallet: GetCurrentWalletUseCase,
  private val ewtObtainer: EwtAuthenticatorService,
) {

  operator fun invoke(): Completable {
    return getCurrentWallet()
      .flatMap { wallet ->
        ewtObtainer.getEwtAuthenticationWithAddress(wallet.address)
      }
      .flatMapCompletable { ewt ->
        cachedGuestWalletRepository.deleteCachedGuestWallet(ewt)
      }
  }

}
