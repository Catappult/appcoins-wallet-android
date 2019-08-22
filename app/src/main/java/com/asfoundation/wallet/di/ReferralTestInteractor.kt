package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.promotions.ReferralsScreen
import com.asfoundation.wallet.promotions.SharedPreferencesReferralLocalData
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function4

class ReferralTestInteractor(
    private val preferences: SharedPreferencesReferralLocalData,
    private val defaultWallet: FindDefaultWalletInteract) : ReferralInteractorContract {

  override fun hasReferralUpdate(screen: ReferralsScreen): Single<Boolean> {
    return defaultWallet.find()
        .flatMap {
          Single.zip(getNumberOfFriends(), getTotalEarned(),
              getSavedNumberOfFriends(it.address, screen),
              getSavedEarned(it.address, screen),
              Function4 { numberOfFriends: Int, totalEarned: String, savedNumberOfFriends: Int, savedTotalEarned: String ->
                hasDifferentInformation(numberOfFriends, totalEarned, savedNumberOfFriends,
                    savedTotalEarned)
              })
        }
  }

  override fun getNumberOfFriends(): Single<Int> {
    return Single.just(2)
  }

  override fun getTotalEarned(): Single<String> {
    return Single.just("$2.5")
  }

  override fun saveReferralInformation(numberOfFriends: Int, totalEarned: String,
                                       screen: ReferralsScreen): Completable {
    return defaultWallet.find()
        .flatMapCompletable {
          saveNumberOfFriends(it.address, numberOfFriends, screen).andThen(
              saveTotalEarned(it.address, totalEarned, screen))
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
