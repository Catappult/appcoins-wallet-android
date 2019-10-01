package com.asfoundation.wallet.referrals

import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface ReferralInteractorContract {

  fun hasReferralUpdate(address: String, friendsInvited: Int, isVerified: Boolean,
                        screen: ReferralsScreen): Single<Boolean>

  fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean>

  fun retrieveReferral(): Single<ReferralsViewModel>

  fun saveReferralInformation(numberOfFriends: Int, isVerified: Boolean,
                              screen: ReferralsScreen): Completable

  fun getReferralNotifications(): Maybe<List<ReferralNotification>>

  fun getPendingBonusNotification(): Maybe<ReferralNotification>

  fun getUnwatchedPendingBonusNotification(): Maybe<ReferralNotification>

  fun dismissNotification(referralNotification: ReferralNotification): Completable

  fun getReferralInfo(): Single<ReferralResponse>
}
