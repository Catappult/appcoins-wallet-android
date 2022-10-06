package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

interface VipReferralPollingRepository {
  fun getLastGamificationStatus(): String
  fun saveLastGamificationStatus(count: String)

  companion object {
    internal const val GAMIFICATION_STATUS = "VipReferral.GamificationStatus"
  }
}

@BoundTo(supertype = VipReferralPollingRepository::class)
class VipReferralPollingRepositoryImpl @Inject constructor() : VipReferralPollingRepository {
  override fun getLastGamificationStatus(): String {
    //TODO
    return GamificationStatus.APPROACHING_VIP.name
  }

  override fun saveLastGamificationStatus(count: String) {
    //TODO

  }

}