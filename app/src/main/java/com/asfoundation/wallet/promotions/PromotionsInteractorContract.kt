package com.asfoundation.wallet.promotions

import io.reactivex.Completable
import io.reactivex.Single

interface PromotionsInteractorContract {

  fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean>

  fun saveReferralInformation(screen: ReferralsScreen): Completable

  fun retrievePromotions(): Single<List<PromotionType>>

  enum class PromotionType {
    REFERRAL, GAMIFICATION
  }
}
