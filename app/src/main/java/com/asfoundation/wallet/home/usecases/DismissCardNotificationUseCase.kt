package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.backup.BackupInteractContract
import com.asfoundation.wallet.backup.BackupNotification
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.interact.UpdateNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralNotification
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DismissCardNotificationUseCase(private val referralInteractor: ReferralInteractorContract,
                                     private val autoUpdateInteract: AutoUpdateInteract,
                                     private val backupInteract: BackupInteractContract,
                                     private val promotionsInteractor: PromotionsInteractor) {

  operator fun invoke(cardNotification: CardNotification): Completable {
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