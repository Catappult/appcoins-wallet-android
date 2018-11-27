package com.appcoins.wallet.gamification.repository

interface GamificationRepository {
  fun getUserStatus(wallet: String)
  fun getLevels()
}
