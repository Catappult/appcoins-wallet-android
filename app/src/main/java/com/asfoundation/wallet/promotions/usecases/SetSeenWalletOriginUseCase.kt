package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.UserStatsLocalData

class SetSeenWalletOriginUseCase(private val userStatsPreferencesRepository: UserStatsLocalData) {

  operator fun invoke(wallet: String, walletOrigin: String) {
    userStatsPreferencesRepository.setSeenWalletOrigin(wallet, walletOrigin)
  }
}