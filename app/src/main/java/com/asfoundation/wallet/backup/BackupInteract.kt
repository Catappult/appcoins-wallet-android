package com.asfoundation.wallet.backup

import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.FetchTransactionsUseCase
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function4
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BackupInteract(
    private val sharedPreferencesRepository: PreferencesRepositoryType,
    private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
    private val fetchTransactionsUseCase: FetchTransactionsUseCase,
    private val balanceInteractor: BalanceInteractor,
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
                if (it) backupRestorePreferencesRepository.setHasShownBackup(wallet.address, it)
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
            backupRestorePreferencesRepository.setBackupNotificationSeenTime(it.address,
                System.currentTimeMillis())
          }
        }
  }

  private fun getBackupThreshold(walletAddress: String): Single<Boolean> {
    val walletRestoreBackup =
        backupRestorePreferencesRepository.isWalletRestoreBackup(walletAddress)
    val previouslyShownBackup = backupRestorePreferencesRepository.hasShownBackup(walletAddress)
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
    return balanceInteractor.requestTokenConversion()
        .firstOrError()
        .map { it.overallFiat.amount >= BigDecimal(BALANCE_AMOUNT_THRESHOLD) }
        .onErrorReturn { false }
  }

  private fun meetsGamificationConditions(): Single<Boolean> {
    // note - this logic should be changed in the future to become offline first
    //  this is to be addressed in another ticket
    return gamificationInteractor.getUserLevel()
        .map { it + 1 >= GAMIFICATION_LEVEL_THRESHOLD }
        .onErrorReturn { false }
  }

  private fun meetsTransactionsCountConditions(walletAddress: String): Single<Boolean> {
    return fetchTransactionsUseCase(walletAddress)
        .map { it.size >= TRANSACTION_COUNT_THRESHOLD }
        .firstOrError()
        .onErrorReturn { false }
  }

  private fun meetsLastDismissConditions(walletAddress: String): Single<Boolean> {
    return Single.create {
      val savedTime =
          backupRestorePreferencesRepository.getBackupNotificationSeenTime(walletAddress)
      val currentTime = System.currentTimeMillis()
      val result = currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
      it.onSuccess(result)
    }
  }

  override fun shouldShowSystemNotification(walletAddress: String): Boolean {
    val hasRestoredBackup = backupRestorePreferencesRepository.isWalletRestoreBackup(walletAddress)
    val count = sharedPreferencesRepository.getWalletPurchasesCount(walletAddress)
    return if (hasRestoredBackup.not() && count > 0 && count % PURCHASE_NOTIFICATION_THRESHOLD == 0) {
      backupRestorePreferencesRepository.hasDismissedBackupSystemNotification(walletAddress)
          .not()
    } else {
      false
    }
  }

  override fun updateWalletPurchasesCount(walletAddress: String): Completable {
    val hasRestoredBackup = backupRestorePreferencesRepository.isWalletRestoreBackup(walletAddress)
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
    backupRestorePreferencesRepository.setDismissedBackupSystemNotification(walletAddress)
  }

}
