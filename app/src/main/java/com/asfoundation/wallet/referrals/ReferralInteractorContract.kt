package com.asfoundation.wallet.referrals

import io.reactivex.Completable
import io.reactivex.Single

interface ReferralInteractorContract {
  fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean>

  fun getNumberOfFriends(): Single<Int>

  fun getTotalEarned(): Single<String>

  fun saveReferralInformation(numberOfFriends: Int, totalEarned: String,
                              screen: ReferralsScreen): Completable

  fun getTotalReferralBonus(): Single<String>

  fun getSingleReferralBonus(): Single<String>

  fun getPendingBonus(): Single<String>
}
