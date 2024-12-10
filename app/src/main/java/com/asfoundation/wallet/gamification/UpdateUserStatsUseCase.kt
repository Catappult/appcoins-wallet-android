package com.asfoundation.wallet.gamification

import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.gamification.Gamification
import com.wallet.appcoins.feature.support.data.SupportRepository
import io.reactivex.Single
import javax.inject.Inject

class UpdateUserStatsUseCase @Inject constructor(
  private val gamification: Gamification,
  private val walletRepository: WalletRepositoryType,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val supportRepository: SupportRepository
) {

  operator fun invoke() = Single.zip(
    getCurrentPromoCodeUseCase(),
    walletRepository.getDefaultWallet()
  ) { promoCode, defaultWallet -> defaultWallet to promoCode }
    .flatMap { (defaultWallet, promoCode) ->
      Single.zip(
        Single.just(defaultWallet),
        gamification.getUserStats(
          wallet = defaultWallet.address,
          promoCodeString = promoCode.code,
          offlineFirst = false
        ).firstOrError()
      ) { wallet, userStats -> wallet.address to userStats }
    }
    .flatMapCompletable { (walletAddress, userStats) -> supportRepository.updateUser(walletAddress, userStats.level) }
}