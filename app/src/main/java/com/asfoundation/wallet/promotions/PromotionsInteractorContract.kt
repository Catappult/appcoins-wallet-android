package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Completable
import io.reactivex.Single

interface PromotionsInteractorContract {

  fun retrievePromotions(): Single<PromotionsModel>

  fun saveReferralInformation(friendsInvited: Int, isVerified: Boolean,
                              screen: ReferralsScreen): Completable

  fun hasReferralUpdate(friendsInvited: Int, isVerified: Boolean,
                        screen: ReferralsScreen): Single<Boolean>
}
