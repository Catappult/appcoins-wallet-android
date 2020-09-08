package com.asfoundation.wallet.interact

import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.backup.BackupNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.promotions.PromotionsInteractorContract
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralNotification
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function4

class CardNotificationsInteractor(
    private val referralInteractor: ReferralInteractorContract,
    private val autoUpdateInteract: AutoUpdateInteract,
    private val backupInteract: BackupInteractContract,
    private val promotionsInteractorContract: PromotionsInteractorContract) {


  fun getCardNotifications(): Single<List<CardNotification>> {
    return Single.zip(referralInteractor.getUnwatchedPendingBonusNotification(),
        autoUpdateInteract.getUnwatchedUpdateNotification(),
        backupInteract.getUnwatchedBackupNotification(),
        promotionsInteractorContract.getUnwatchedPromotionNotification(),
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
      is PromotionNotification -> promotionsInteractorContract.dismissNotification()
      else -> Completable.complete()
    }
  }
}