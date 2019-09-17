package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class PromotionsInteractor(private val referralInteractor: ReferralInteractorContract,
                           private val promotionsRepo: PromotionsRepository,
                           private val findWalletInteract: FindDefaultWalletInteract) :
    PromotionsInteractorContract {

  override fun hasReferralUpdate(friendsInvited: Int, receivedValue: BigDecimal,
                                 isVerified: Boolean, screen: ReferralsScreen): Single<Boolean> {
    return findWalletInteract.find()
        .flatMap {
          referralInteractor.hasReferralUpdate(it.address, friendsInvited, receivedValue,
              isVerified, screen)
        }
  }

  override fun saveReferralInformation(friendsInvited: Int, receivedValue: BigDecimal,
                                       isVerified: Boolean, screen: ReferralsScreen): Completable {
    return referralInteractor.saveReferralInformation(friendsInvited, receivedValue.toString(),
        isVerified, screen)

  }

  override fun retrievePromotions(): Single<PromotionsViewModel> {
    return findWalletInteract.find()
        .flatMap { promotionsRepo.getUserStatus(it.address) }
        .map { map(it) }
  }

  private fun map(userStatus: UserStatusResponse): PromotionsViewModel {
    val gamification = userStatus.gamification
    val referral = userStatus.referral
    val maxAmount =
        referral.amount.multiply(BigDecimal(referral.available.plus(referral.completed)))
    return PromotionsViewModel(gamification.bundle, referral.bundle,
        gamification.level, gamification.nextLevelAmount, gamification.totalSpend,
        gamification.status, referral.link, maxAmount, referral.completed,
        referral.receivedAmount, referral.link != null, referral.symbol)
  }
}