package com.asfoundation.wallet.backup

import com.asf.wallet.R
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.interact.FetchTransactionsInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function4
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BackupInteract(
    private val sharedPreferencesRepository: PreferencesRepositoryType,
    private val fetchTransactionsInteract: FetchTransactionsInteract,
    private val balanceInteract: BalanceInteract,
    private val gamificationInteractor: GamificationInteractor,
    private val findDefaultWalletInteract: FindDefaultWalletInteract
) : BackupInteractContract {

  companion object {
    private const val DISMISS_PERIOD = 30L
    private const val TRANSACTION_COUNT_THRESHOLD = 10
    private const val GAMIFICATION_LEVEL_THRESHOLD = 2
    private const val BALANCE_AMOUNT_THRESHOLD = 10
    private const val PURCHASE_NOTIFICATION_THRESHOLD = 2
  }

  override fun getUnwatchedBackupNotification(): Single<CardNotification> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          getBackupThreshold(wallet.address)
              .doOnSuccess {
                if (it) sharedPreferencesRepository.setHasShownBackup(wallet.address, it)
              }
              .map { shouldShow ->
                BackupNotification(
                    R.string.backup_home_notification_title,
                    R.string.backup_home_notification_body,
                    R.drawable.ic_backup_notification,
                    R.string.backup_button,
                    CardNotificationAction.BACKUP).takeIf { shouldShow } ?: EmptyNotification()
              }
        }
  }

  override fun dismissNotification(): Completable {
    return findDefaultWalletInteract.find()
        .flatMapCompletable {
          Completable.fromAction {
            sharedPreferencesRepository.setBackupNotificationSeenTime(it.address,
                System.currentTimeMillis())
          }
        }
  }

  private fun getBackupThreshold(walletAddress: String): Single<Boolean> {
    val walletRestoreBackup = sharedPreferencesRepository.isWalletRestoreBackup(walletAddress)
    val previouslyShownBackup = sharedPreferencesRepository.hasShownBackup(walletAddress)
    return if (walletRestoreBackup) {
      Single.just(false)
    } else {
      Single.zip(
          meetsLastDismissConditions(walletAddress),
          meetsTransactionsCountConditions(walletAddress),
          meetsGamificationConditions(),
          meetsBalanceConditions(),
          Function4 { dismissPeriodGone, transactions, gamification, balance ->
            (previouslyShownBackup || transactions || gamification || balance) && dismissPeriodGone
          }
      )
    }
  }

  private fun meetsBalanceConditions(): Single<Boolean> {
    return balanceInteract.requestTokenConversion()
        .firstOrError()
        .map { it.overallFiat.amount >= BigDecimal(BALANCE_AMOUNT_THRESHOLD) }
        .onErrorReturn { false }
  }

  private fun meetsGamificationConditions(): Single<Boolean> {
    return gamificationInteractor.getUserStats()
        .map { it.level + 1 >= GAMIFICATION_LEVEL_THRESHOLD }
        .onErrorReturn { false }
  }

  private fun meetsTransactionsCountConditions(walletAddress: String): Single<Boolean> {
    return fetchTransactionsInteract.fetch(walletAddress)
        .doAfterTerminate { fetchTransactionsInteract.stop() }
        .map { it.size >= TRANSACTION_COUNT_THRESHOLD }
        .firstOrError()
        .onErrorReturn { false }
  }

  private fun meetsLastDismissConditions(walletAddress: String): Single<Boolean> {
    return Single.create {
      val savedTime = sharedPreferencesRepository.getBackupNotificationSeenTime(walletAddress)
      val currentTime = System.currentTimeMillis()
      val result = currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
      it.onSuccess(result)
    }
  }

  override fun shouldShowSystemNotification(walletAddress: String): Boolean {
    val hasRestoredBackup = sharedPreferencesRepository.isWalletRestoreBackup(walletAddress)
    val count = sharedPreferencesRepository.getWalletPurchasesCount(walletAddress)
    return if (hasRestoredBackup.not() && count > 0 && count % PURCHASE_NOTIFICATION_THRESHOLD == 0) {
      sharedPreferencesRepository.hasDismissedBackupSystemNotification(walletAddress)
          .not()
    } else {
      false
    }
  }

  override fun updateWalletPurchasesCount(walletAddress: String): Completable {
    val hasRestoredBackup = sharedPreferencesRepository.isWalletRestoreBackup(walletAddress)
    return if (hasRestoredBackup.not()) {
      Single.just(sharedPreferencesRepository.getWalletPurchasesCount(walletAddress))
          .map { it + 1 }
          .flatMapCompletable {
            sharedPreferencesRepository.incrementWalletPurchasesCount(walletAddress, it)
          }
    } else {
      Completable.complete()
    }
  }

  override fun saveDismissSystemNotification(walletAddress: String) {
    sharedPreferencesRepository.setDismissedBackupSystemNotification(walletAddress)
  }

}
