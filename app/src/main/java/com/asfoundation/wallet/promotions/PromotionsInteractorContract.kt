package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

interface PromotionsInteractorContract {

  fun retrievePromotions(): Single<PromotionsViewModel>

  fun saveReferralInformation(friendsInvited: Int, receivedValue: BigDecimal, isVerified: Boolean,
                              screen: ReferralsScreen): Completable

  fun hasReferralUpdate(friendsInvited: Int, receivedValue: BigDecimal, isVerified: Boolean,
                        screen: ReferralsScreen): Single<Boolean>
}
