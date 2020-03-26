package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.Status
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.math.RoundingMode

class PromotionsInteractor(private val referralInteractor: ReferralInteractorContract,
                           private val gamificationInteractor: GamificationInteractor,
                           private val promotionsRepo: PromotionsRepository,
                           private val findWalletInteract: FindDefaultWalletInteract) :
    PromotionsInteractorContract {

  override fun retrievePromotions(): Single<PromotionsModel> {
    return findWalletInteract.find()
        .flatMap { promotionsRepo.getUserStatus(it.address) }
        .map { map(it) }
  }

  override fun hasAnyPromotionUpdate(referralsScreen: ReferralsScreen,
                                     gamificationScreen: GamificationScreen): Single<Boolean> {
    return retrievePromotions()
        .flatMap {
          when {
            it.gamificationAvailable && it.referralsAvailable -> {
              Single.zip(
                  hasReferralUpdate(it.numberOfInvitations, it.isValidated,
                      ReferralsScreen.PROMOTIONS),
                  gamificationInteractor.hasNewLevel(GamificationScreen.PROMOTIONS),
                  BiFunction { hasReferralUpdate: Boolean, hasNewLevel: Boolean ->
                    hasReferralUpdate || hasNewLevel
                  })
            }
            it.gamificationAvailable -> gamificationInteractor.hasNewLevel(
                GamificationScreen.PROMOTIONS)
            it.referralsAvailable -> hasReferralUpdate(it.numberOfInvitations, it.isValidated,
                ReferralsScreen.PROMOTIONS)
            else -> Single.just(false)
          }
        }
  }

  //Referrals

  override fun hasReferralUpdate(friendsInvited: Int, isVerified: Boolean,
                                 screen: ReferralsScreen): Single<Boolean> {
    return findWalletInteract.find()
        .flatMap {
          referralInteractor.hasReferralUpdate(it.address, friendsInvited, isVerified, screen)
        }
  }

  override fun saveReferralInformation(friendsInvited: Int, isVerified: Boolean,
                                       screen: ReferralsScreen): Completable {
    return referralInteractor.saveReferralInformation(friendsInvited, isVerified, screen)
  }

  //Gamification

  override fun hasGamificationNewLevel(screen: GamificationScreen): Single<Boolean> {
    return gamificationInteractor.hasNewLevel(screen)
  }

  override fun retrieveGamificationRewardStatus(
      screen: GamificationScreen): Single<UserRewardsStatus> {
    return Single.zip(
        gamificationInteractor.getLevels(),
        gamificationInteractor.getUserStats(),
        gamificationInteractor.getLastShownLevel(GamificationScreen.PROMOTIONS),
        Function3 { levels: Levels, userStats: UserStats, lastShownLevel: Int ->
          mapToUserStatus(levels, userStats, lastShownLevel)
        })
  }

  override fun levelShown(level: Int, promotions: GamificationScreen): Completable {
    return gamificationInteractor.levelShown(level, promotions)
  }

  private fun mapToUserStatus(levels: Levels, userStats: UserStats,
                              lastShownLevel: Int): UserRewardsStatus {
    var status = Status.OK
    if (levels.status == Levels.Status.NO_NETWORK && userStats.status == UserStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      var list = listOf<Double>()
      if (levels.isActive) {
        list = levels.list.map { it.bonus }
      }

      val maxBonus = list.max()
          ?.toString() ?: "0.0"
      val nextLevelAmount = userStats.nextLevelAmount?.minus(
          userStats.totalSpend)
          ?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      return UserRewardsStatus(lastShownLevel, userStats.level, nextLevelAmount, list, status,
          maxBonus)
    }
    return UserRewardsStatus(lastShownLevel, lastShownLevel, status = status)
  }

  private fun map(userStatus: UserStatusResponse): PromotionsModel {
    val gamification = userStatus.gamification
    val referral = userStatus.referral
    val gamificationAvailable =
        gamification.bundle && gamification.status == GamificationResponse.Status.ACTIVE
    val referralsAvailable = referral.bundle && referral.status == ReferralResponse.Status.ACTIVE
    val maxAmount =
        referral.amount.multiply(BigDecimal(referral.available.plus(referral.completed)))
    return PromotionsModel(gamificationAvailable, referralsAvailable, gamification.level,
        gamification.nextLevelAmount, gamification.totalSpend,
        referral.link, maxAmount, referral.completed, referral.receivedAmount,
        referral.link != null, referral.symbol)
  }
}