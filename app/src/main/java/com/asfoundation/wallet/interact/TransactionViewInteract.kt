package com.asfoundation.wallet.interact

import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.util.Pair
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractorContract
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.FingerprintInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single


class TransactionViewInteract(private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
                              private val findDefaultWalletInteract: FindDefaultWalletInteract,
                              private val fetchTransactionsInteract: FetchTransactionsInteract,
                              private val gamificationInteractor: GamificationInteractor,
                              private val balanceInteractor: BalanceInteractor,
                              private val promotionsInteractor: PromotionsInteractorContract,
                              private val cardNotificationsInteractor: CardNotificationsInteractor,
                              private val autoUpdateInteract: AutoUpdateInteract,
                              private val preferencesRepositoryType: PreferencesRepositoryType,
                              private val packageManager: PackageManager,
                              private val fingerprintInteractor: FingerprintInteractor,
                              private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  private companion object {
    private const val UPDATE_FINGERPRINT_NUMBER_OF_TIMES = 3
  }

  val levels: Single<Levels>
    get() = gamificationInteractor.getLevels()

  val appcBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteractor.getAppcBalance()

  val ethereumBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteractor.getEthBalance()

  val creditsBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteractor.getCreditsBalance()

  val cardNotifications: Single<List<CardNotification>>
    get() = cardNotificationsInteractor.getCardNotifications()

  val userLevel: Single<Int>
    get() = gamificationInteractor.getUserStats()
        .map { it.level }

  fun findNetwork(): Single<NetworkInfo> {
    return findDefaultNetworkInteract.find()
  }

  fun hasPromotionUpdate(): Single<Boolean> {
    return promotionsInteractor.hasAnyPromotionUpdate(ReferralsScreen.PROMOTIONS,
        GamificationScreen.PROMOTIONS, PromotionUpdateScreen.PROMOTIONS)
  }

  fun fetchTransactions(wallet: Wallet?): Observable<List<Transaction>> {
    return wallet?.let { fetchTransactionsInteract.fetch(wallet.address) } ?: Observable.just(
        emptyList())
  }

  fun stopTransactionFetch() = fetchTransactionsInteract.stop()

  fun findWallet(): Single<Wallet> {
    return findDefaultWalletInteract.find()
  }

  fun dismissNotification(cardNotification: CardNotification): Completable {
    return cardNotificationsInteractor.dismissNotification(cardNotification)
  }

  fun retrieveUpdateIntent() = autoUpdateInteract.buildUpdateIntent()

  fun hasSeenPromotionTooltip(): Single<Boolean> =
      Single.just(preferencesRepositoryType.hasSeenPromotionTooltip())

  fun increaseTimesOnHome() {
    if (preferencesRepositoryType.getNumberOfTimesOnHome() <= UPDATE_FINGERPRINT_NUMBER_OF_TIMES) {
      preferencesRepositoryType.increaseTimesOnHome()
    }
  }

  private fun getNumberOfTimesOnHome(): Int = preferencesRepositoryType.getNumberOfTimesOnHome()

  fun shouldShowFingerprintTooltip(packageName: String): Single<Boolean> {
    var shouldShow = false
    if (!preferencesRepositoryType.hasBeenInSettings() && !fingerprintPreferences.hasSeenFingerprintTooltip()
        && hasFingerprint() && !fingerprintPreferences.hasAuthenticationPermission() &&
        preferencesRepositoryType.hasSeenPromotionTooltip()) {
      if (!isFirstInstall(packageName)) {
        shouldShow = true
      } else if (getNumberOfTimesOnHome() >= UPDATE_FINGERPRINT_NUMBER_OF_TIMES) {
        shouldShow = true
      }
    }
    return Single.just(shouldShow)
  }

  fun setSeenFingerprintTooltip() = fingerprintPreferences.setSeenFingerprintTooltip()

  private fun isFirstInstall(packageName: String): Boolean {
    return try {
      val firstInstallTime: Long = packageManager.getPackageInfo(packageName, 0).firstInstallTime
      val lastUpdateTime: Long = packageManager.getPackageInfo(packageName, 0).lastUpdateTime
      firstInstallTime == lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
      true
    }
  }

  private fun hasFingerprint(): Boolean {
    return fingerprintInteractor.getDeviceCompatibility() == BiometricManager.BIOMETRIC_SUCCESS
  }
}
