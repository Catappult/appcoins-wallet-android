package com.asfoundation.wallet.promotions

import io.reactivex.Single
import java.io.IOException

//TODO Remove when real implementation is created
class PromotionsTestInteractor {

  private var boolean: Boolean = false

  fun hasReferralUpdate(): Single<Boolean> {
    return Single.just(true)
  }

  fun retrievePromotions(): Single<List<PromotionType>> {
    return if (boolean) {
      Single.just(listOf(PromotionType.REFERRAL, PromotionType.GAMIFICATION))
    } else {
      boolean = true
      return Single.error(IOException())
    }
  }

  enum class PromotionType {
    REFERRAL, GAMIFICATION
  }
}
