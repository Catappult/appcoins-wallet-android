package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetCardNotificationsUseCase(private val referralInteractor: ReferralInteractorContract,
                                  private val autoUpdateInteract: AutoUpdateInteract,
                                  private val backupInteract: BackupInteractContract,
                                  private val promotionsInteractor: PromotionsInteractor) {

  operator fun invoke(): Single<List<CardNotification>> {
    val getUnwatchedPendingBonusNotification =
        referralInteractor.getUnwatchedPendingBonusNotification()
            .subscribeOn(Schedulers.io())
    val getUnwatchedUpdateNotification = autoUpdateInteract.getUnwatchedUpdateNotification()
        .subscribeOn(Schedulers.io())
    val getUnwatchedBackupNotification = backupInteract.getUnwatchedBackupNotification()
        .subscribeOn(Schedulers.io())
    val getUnwatchedPromotionNotification = promotionsInteractor.getUnwatchedPromotionNotification()
        .subscribeOn(Schedulers.io())
    return Single.zip(getUnwatchedPendingBonusNotification, getUnwatchedUpdateNotification,
        getUnwatchedBackupNotification, getUnwatchedPromotionNotification,
        { referralNotification: CardNotification, updateNotification: CardNotification, backupNotification: CardNotification, promotionNotification: CardNotification ->
          val list = ArrayList<CardNotification>()
          if (referralNotification !is EmptyNotification) list.add(referralNotification)
          if (backupNotification !is EmptyNotification) list.add(backupNotification)
          if (updateNotification !is EmptyNotification) list.add(updateNotification)
          if (promotionNotification !is EmptyNotification) list.add(promotionNotification)
          list
        })
  }
}