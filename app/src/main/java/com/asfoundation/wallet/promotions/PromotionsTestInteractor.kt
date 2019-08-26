package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.promotions.PromotionsInteractorContract.PromotionType
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.IOException

class PromotionsTestInteractor(private val referralInteractor: ReferralInteractorContract) :
    PromotionsInteractorContract {

  private var boolean: Boolean = false
  private val showError: Boolean = false

  override fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean> {
    return referralInteractor.hasReferralUpdate(screen)
  }

  override fun saveReferralInformation(screen: ReferralsScreen): Completable {
    return Single.zip(referralInteractor.getNumberOfFriends(),
        referralInteractor.getTotalEarned(),
        BiFunction { numberOfFriends: Int, totalEarned: String ->
          Pair(numberOfFriends, totalEarned)
        })
        .flatMapCompletable {
          referralInteractor.saveReferralInformation(it.first, it.second, screen)
        }
  }

  override fun retrievePromotions(): Single<List<PromotionType>> {
    return if (boolean || !showError) {
      Single.just(listOf(PromotionType.REFERRAL, PromotionType.GAMIFICATION))
    } else {
      boolean = true
      return Single.error(IOException())
    }
  }

  override fun retrieveReferralBonus(): Single<String> {
    return Single.just("$10")
  }
}
