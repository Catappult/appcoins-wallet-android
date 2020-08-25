package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Single

interface PromotionsInteractorContract {

  fun retrievePromotions(): Single<PromotionsModel>

  fun hasAnyPromotionUpdate(referralsScreen: ReferralsScreen,
                            gamificationScreen: GamificationScreen): Single<Boolean>

}
