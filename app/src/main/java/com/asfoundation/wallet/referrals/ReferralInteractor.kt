package com.asfoundation.wallet.referrals

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.util.scaleToString
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.math.BigDecimal

class ReferralInteractor(
    private val preferences: SharedPreferencesReferralLocalData,
    private val defaultWallet: FindDefaultWalletInteract,
    private val promotionsRepository: PromotionsRepository) :
    ReferralInteractorContract {

  override fun hasReferralUpdate(address: String, friendsInvited: Int, receivedValue: BigDecimal,
                                 isVerified: Boolean, screen: ReferralsScreen): Single<Boolean> {
    return getReferralInformation(address, screen)
        .map {
          hasDifferentInformation(receivedValue.toString() + friendsInvited + isVerified, it)
        }
  }

  override fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean> {
    return defaultWallet.find()
        .flatMap { wallet ->
          promotionsRepository.getReferralUserStatus(wallet.address)
              .flatMap {
                hasReferralUpdate(wallet.address, it.completed, it.receivedAmount,
                    it.link != null, screen)
              }
        }
  }

  override fun retrieveReferral(): Single<ReferralResponse> {
    return defaultWallet.find()
        .flatMap { promotionsRepository.getReferralUserStatus(it.address) }
  }

  override fun saveReferralInformation(numberOfFriends: Int, totalEarned: String,
                                       isVerified: Boolean, screen: ReferralsScreen): Completable {
    return defaultWallet.find()
        .flatMapCompletable {
          saveReferralInformation(it.address, totalEarned, numberOfFriends, isVerified, screen)
        }
  }

  private fun getReferralInformation(address: String, screen: ReferralsScreen): Single<String> {
    return preferences.getReferralInformation(address, screen.toString())
  }

  private fun saveReferralInformation(address: String, totalEarned: String, numberOfFriends: Int,
                                      isVerified: Boolean,
                                      screen: ReferralsScreen): Completable {
    return preferences.saveReferralInformation(address, totalEarned, numberOfFriends, isVerified,
        screen.toString())
  }

  private fun hasDifferentInformation(newInformation: String, savedInformation: String): Boolean {
    return newInformation != savedInformation
  }

  override fun getReferralNotifications(): Maybe<List<ReferralNotification>> {
    return getPendingBonusNotification().map { listOf(it) }
  }

  private fun getPendingBonusNotification(): Maybe<ReferralNotification> {
    return defaultWallet.find()
        .flatMapMaybe { wallet ->
          promotionsRepository.getReferralUserStatus(wallet.address)
              .flatMapMaybe { userStats ->
                preferences.getPendingAmountNotification(wallet.address)
                    .filter {
                      userStats.pendingAmount.compareTo(BigDecimal.ZERO) != 0 &&
                          it != userStats.pendingAmount.toPlainString()
                    }
                    .map {
                      ReferralNotification(1, R.string.referral_notification_bonus_pending_title,
                          R.string.referral_notification_bonus_pending_body,
                          R.drawable.ic_bonus_pending,
                          userStats.pendingAmount.scaleToString(2),
                          userStats.symbol)
                    }
              }
        }
  }

  override fun dismissNotification(referralNotification: ReferralNotification): Completable {
    return defaultWallet.find()
        .flatMapCompletable {
          preferences.savePendingAmountNotification(it.address, referralNotification.pendingAmount)
        }
  }

}
