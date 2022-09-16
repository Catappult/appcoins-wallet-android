package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import io.reactivex.Single
import javax.inject.Inject

class SetVipPromotionsSeenUseCase @Inject constructor(
  private val promotionsRepository: PromotionsRepository
) {

  operator fun invoke(isSeen: Boolean) {
    promotionsRepository.setVipCalloutAlreadySeen(isSeen)
  }

}
