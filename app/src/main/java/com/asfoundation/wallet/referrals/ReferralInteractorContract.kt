package com.asfoundation.wallet.referrals

import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface ReferralInteractorContract {

  fun hasReferralUpdate(walletAddress: String,
                        referralResponse: ReferralResponse?,
                        screen: ReferralsScreen): Single<Boolean>

  fun retrieveReferral(): Single<ReferralModel>

  fun saveReferralInformation(numberOfFriends: Int, isVerified: Boolean,
                              screen: ReferralsScreen): Completable

  fun getPendingBonusNotification(): Maybe<ReferralNotification>

  fun getUnwatchedPendingBonusNotification(): Single<CardNotification>

  fun dismissNotification(referralNotification: ReferralNotification): Completable
}
