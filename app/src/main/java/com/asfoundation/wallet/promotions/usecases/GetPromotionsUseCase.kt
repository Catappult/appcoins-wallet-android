package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.gamification.ObserveLevelsUseCase
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable

class GetPromotionsUseCase(private val getCurrentWallet: GetCurrentWalletUseCase,
                           private val observeLevels: ObserveLevelsUseCase,
                           private val promotionsMapper: PromotionsMapper,
                           private val promotionsRepository: PromotionsRepository) {

  operator fun invoke(): Observable<PromotionsModel> {
    return getCurrentWallet()
        .flatMapObservable {
          Observable.zip(observeLevels(), promotionsRepository.getUserStats(it.address),
              { levels: Levels, userStatsResponse: UserStats ->
                promotionsMapper.mapToPromotionsModel(userStatsResponse, levels, it)
              })
        }
  }
}