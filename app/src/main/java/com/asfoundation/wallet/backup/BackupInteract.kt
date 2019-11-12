package com.asfoundation.wallet.backup

import com.asf.wallet.R
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Completable
import io.reactivex.Single

class BackupInteract(
    private val sharedPreferencesRepository: PreferencesRepositoryType) : BackupInteractContract {

  override fun getUnwatchedBackupNotification(): Single<CardNotification> {
    return Single.just(true)
        .map { shouldShow ->
          BackupNotification(
              R.string.referral_notification_bonus_pending_title,
              R.string.referral_notification_bonus_pending_body,
              R.drawable.ic_bonus_pending,
              R.string.gamification_APPCapps_button,
              CardNotificationAction.BACKUP).takeIf { shouldShow } ?: EmptyNotification()
        }
  }

  override fun dismissNotification(): Completable {
    return Completable.fromAction {
      sharedPreferencesRepository.setBackupNotificationSeenTime(System.currentTimeMillis())
    }
  }

}
