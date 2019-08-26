package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Completable
import io.reactivex.Single

interface PromotionsInteractorContract {

  fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean>

  fun saveReferralInformation(screen: ReferralsScreen): Completable

  fun retrievePromotions(): Single<List<PromotionType>>

  fun retrieveReferralBonus(): Single<String>

  enum class PromotionType {
    REFERRAL, GAMIFICATION
  }
}
