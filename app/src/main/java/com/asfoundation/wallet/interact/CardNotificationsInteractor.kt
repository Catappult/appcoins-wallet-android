package com.asfoundation.wallet.interact

import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.backup.BackupNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralNotification
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Function4

class CardNotificationsInteractor(private val referralInteractor: ReferralInteractorContract,
                                  private val autoUpdateInteract: AutoUpdateInteract,
                                  private val backupInteract: BackupInteractContract,
                                  private val promotionsInteractor: PromotionsInteractor,
                                  private val ioScheduler: Scheduler) {


  fun getCardNotifications(): Single<List<CardNotification>> {
    val getUnwatchedPendingBonusNotification =
        referralInteractor.getUnwatchedPendingBonusNotification()
            .subscribeOn(ioScheduler)
    val getUnwatchedUpdateNotification = autoUpdateInteract.getUnwatchedUpdateNotification()
        .subscribeOn(ioScheduler)
    val getUnwatchedBackupNotification = backupInteract.getUnwatchedBackupNotification()
        .subscribeOn(ioScheduler)
    val getUnwatchedPromotionNotification = promotionsInteractor.getUnwatchedPromotionNotification()
        .subscribeOn(ioScheduler)
    return Single.zip(getUnwatchedPendingBonusNotification, getUnwatchedUpdateNotification,
        getUnwatchedBackupNotification, getUnwatchedPromotionNotification,
        Function4 { referralNotification: CardNotification, updateNotification: CardNotification, backupNotification: CardNotification, promotionNotification: CardNotification ->
          val list = ArrayList<CardNotification>()
          if (referralNotification !is EmptyNotification) list.add(referralNotification)
          if (backupNotification !is EmptyNotification) list.add(backupNotification)
          if (updateNotification !is EmptyNotification) list.add(updateNotification)
          if (promotionNotification !is EmptyNotification) list.add(promotionNotification)
          list
        })
  }

  fun dismissNotification(cardNotification: CardNotification): Completable {
    return when (cardNotification) {
      is ReferralNotification -> referralInteractor.dismissNotification(cardNotification)
      is UpdateNotification -> autoUpdateInteract.dismissNotification()
      is BackupNotification -> backupInteract.dismissNotification()
      is PromotionNotification -> promotionsInteractor.dismissNotification(
          cardNotification.id)
      else -> Completable.complete()
    }
  }
}