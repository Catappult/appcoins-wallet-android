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

  override fun getUnwatchedBackupNotification(): Single<CardNotification> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet -> getBackupThreshold(wallet.address) }
        .map { shouldShow ->
          BackupNotification(
              R.string.referral_notification_bonus_pending_title,
              R.string.referral_notification_bonus_pending_body,
              R.drawable.ic_backup_notification,
              R.string.gamification_APPCapps_button,
              CardNotificationAction.BACKUP).takeIf { shouldShow } ?: EmptyNotification()
        }
  }

  override fun dismissNotification(): Completable {
    return Completable.fromAction {
      sharedPreferencesRepository.setBackupNotificationSeenTime(System.currentTimeMillis())
    }
  }

  private fun getBackupThreshold(walletAddress: String): Single<Boolean> {
    val walletImportBackup = sharedPreferencesRepository.isWalletImportBackup(walletAddress)
    return if (walletImportBackup) {
      Single.just(false)
    } else {
      Single.zip(
          meetsLastDismissConditions(),
          meetsTransactionsCountConditions(walletAddress),
          meetsGamificationConditions(),
          meetsBalanceConditions(),
          Function4 { dismiss, transactions, gamification, balance ->
            (transactions || gamification || balance) && dismiss.not()
          }
      )
    }
  }

  private fun meetsBalanceConditions(): Single<Boolean> {
    return balanceInteract.requestTokenConversion()
        .firstOrError()
        .map { it.overallFiat.amount > BigDecimal.TEN }
  }

  private fun meetsGamificationConditions(): Single<Boolean> {
    return gamificationInteractor.getUserStats()
        .map { it.level >= 2 }
  }

  private fun meetsTransactionsCountConditions(walletAddress: String): Single<Boolean> {
    return fetchTransactionsInteract.fetch(walletAddress)
        .doAfterTerminate { fetchTransactionsInteract.stop() }
        .map { transactions -> transactions.size >= 10 }
        .firstOrError()
  }

  private fun meetsLastDismissConditions(): Single<Boolean> {
    return Single.create<Boolean> {
      val savedTime = sharedPreferencesRepository.getBackupNotificationSeenTime()
      val currentTime = System.currentTimeMillis()
      val result = currentTime >= savedTime + TimeUnit.DAYS.toMillis(30)
      it.onSuccess(result)
    }
  }

}
