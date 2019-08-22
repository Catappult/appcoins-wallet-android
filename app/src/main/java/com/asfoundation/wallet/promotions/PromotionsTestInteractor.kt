package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.di.ReferralTestInteractor
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.IOException

//TODO Remove when real implementation is created
class PromotionsTestInteractor(private val referralInteractor: ReferralTestInteractor) {

  private var boolean: Boolean = false
  private val showError: Boolean = false

  fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean> {
    return referralInteractor.hasReferralUpdate(screen)
  }

  fun saveReferralInformation(screen: ReferralsScreen): Completable {
    return Single.zip(referralInteractor.getNumberOfFriends(),
        referralInteractor.getTotalEarned(),
        BiFunction { numberOfFriends: Int, totalEarned: String ->
          Pair(numberOfFriends, totalEarned)
        })
        .flatMapCompletable {
          referralInteractor.saveReferralInformation(it.first, it.second, screen)
        }
  }

  fun retrievePromotions(): Single<List<PromotionType>> {
    return if (boolean || !showError) {
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
