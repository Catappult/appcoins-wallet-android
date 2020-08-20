package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Completable
import io.reactivex.Single

interface PromotionsInteractorContract {

  fun retrievePromotions(): Single<PromotionsModel>

  fun hasAnyPromotionUpdate(referralsScreen: ReferralsScreen,
                            gamificationScreen: GamificationScreen): Single<Boolean>

  fun saveReferralInformation(friendsInvited: Int, isVerified: Boolean,
                              screen: ReferralsScreen): Completable

  fun hasReferralUpdate(friendsInvited: Int, isVerified: Boolean,
                        screen: ReferralsScreen): Single<Boolean>

  fun hasGamificationNewLevel(screen: GamificationScreen): Single<Boolean>

  fun levelShown(level: Int, promotions: GamificationScreen): Completable
}
