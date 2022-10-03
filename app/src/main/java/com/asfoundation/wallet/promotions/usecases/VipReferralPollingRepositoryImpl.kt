package com.asfoundation.wallet.promotions.usecases

interface VipReferralPollingRepository {
  fun getLastGamificationStatus(): String
  fun saveLastGamificationStatus(count: String)

  companion object {
    internal const val GAMIFICATION_STATUS = "VipReferral.GamificationStatus"
  }
}

class VipReferralPollingRepositoryImpl : VipReferralPollingRepository {
  override fun getLastGamificationStatus(): String {
    TODO("Not yet implemented")
  }

  override fun saveLastGamificationStatus(count: String) {
    TODO("Not yet implemented")
  }

}