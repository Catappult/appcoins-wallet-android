package com.appcoins.wallet.gamification.repository

class BdsGamificationRepository(private val api: GamificationApi) :
    GamificationRepository {
  override fun getUserStatus(wallet: String) {
    api.getUserStatus(wallet)
  }

  override fun getLevels() {
    api.getLevels()
  }
}