package com.asfoundation.wallet.referrals

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal

class ReferralInteractor(
    private val preferences: SharedPreferencesReferralLocalData,
    private val defaultWallet: FindDefaultWalletInteract,
    private val promotionsRepository: PromotionsRepository) :
    ReferralInteractorContract {

  override fun hasReferralUpdate(address: String, friendsInvited: Int, receivedValue: BigDecimal,
                                 screen: ReferralsScreen): Single<Boolean> {
    return Single.zip(getSavedNumberOfFriends(address, screen),
        getSavedEarned(address, screen),
        BiFunction { savedNumberOfFriends: Int, savedTotalEarned: String ->
          hasDifferentInformation(friendsInvited, receivedValue.toString(),
              savedNumberOfFriends, savedTotalEarned)
        })
  }

  override fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean> {
    return defaultWallet.find()
        .flatMap { wallet ->
          promotionsRepository.getReferralUserStatus(wallet.address)
              .flatMap { hasReferralUpdate(wallet.address, it.invited, it.receivedAmount, screen) }
        }
  }

  override fun retrieveReferral(): Single<ReferralResponse> {
    return defaultWallet.find()
        .flatMap { promotionsRepository.getReferralUserStatus(it.address) }
  }

  override fun saveReferralInformation(numberOfFriends: Int, totalEarned: String,
                                       screen: ReferralsScreen): Completable {
    return defaultWallet.find()
        .flatMapCompletable {
          saveNumberOfFriends(it.address, numberOfFriends, screen)
              .andThen(saveTotalEarned(it.address, totalEarned, screen))
        }
  }

  private fun getSavedNumberOfFriends(address: String, screen: ReferralsScreen): Single<Int> {
    return preferences.getNumberOfFriends(address, screen.toString())
  }

  private fun getSavedEarned(address: String, screen: ReferralsScreen): Single<String> {
    return preferences.getEarned(address, screen.toString())

  }

  private fun saveNumberOfFriends(address: String, numberOfFriends: Int,
                                  screen: ReferralsScreen): Completable {
    return preferences.saveNumberOfFriends(address, numberOfFriends, screen.toString())
  }

  private fun saveTotalEarned(address: String, totalEarned: String,
                              screen: ReferralsScreen): Completable {
    return preferences.saveTotalEarned(address, totalEarned, screen.toString())
  }

  private fun hasDifferentInformation(numberOfFriends: Int, totalEarned: String,
                                      savedNumberOfFriends: Int,
                                      savedTotalEarned: String): Boolean {
    return numberOfFriends != savedNumberOfFriends || totalEarned != savedTotalEarned
  }
}
