package com.asfoundation.wallet.referrals

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface ReferralInteractorContract {

  fun hasReferralUpdate(friendsInvited: Int, isVerified: Boolean,
                        screen: ReferralsScreen): Single<Boolean>

  fun retrieveReferral(): Single<ReferralModel>

  fun saveReferralInformation(numberOfFriends: Int, isVerified: Boolean,
                              screen: ReferralsScreen): Completable

  fun getPendingBonusNotification(): Maybe<ReferralNotification>

  fun getReferralInfo(): Single<ReferralModel>

  fun getUnwatchedPendingBonusNotification(): Single<CardNotification>

  fun dismissNotification(referralNotification: ReferralNotification): Completable
}
