package com.asfoundation.wallet.promotions

import io.reactivex.Single
import java.io.IOException

//TODO Remove when real implementation is created
class PromotionsTestInteractor {

  private var boolean: Boolean = false

  fun hasReferralUpdate(): Single<Boolean> {
    return Single.just(true)
  }

  fun retrievePromotions(): Single<List<String>> {
    return if (boolean) {
      Single.just(listOf("Referral", "Gamification"))
    } else {
      boolean = true
      return Single.error(IOException())
    }
  }
}
