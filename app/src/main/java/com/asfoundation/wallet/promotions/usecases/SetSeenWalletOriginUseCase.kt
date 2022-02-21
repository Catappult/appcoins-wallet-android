package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import javax.inject.Inject

class SetSeenWalletOriginUseCase @Inject constructor(
    private val userStatsPreferencesRepository: UserStatsLocalData) {

  operator fun invoke(wallet: String, walletOrigin: String) {
    userStatsPreferencesRepository.setSeenWalletOrigin(wallet, walletOrigin)
  }
}