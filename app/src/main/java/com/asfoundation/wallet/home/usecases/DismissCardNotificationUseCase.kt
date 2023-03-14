package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.interact.UpdateNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralNotification
import referrals.SharedPreferencesReferralLocalData
import com.asfoundation.wallet.repository.AutoUpdateRepository
import repository.PreferencesRepositoryType
import com.asfoundation.wallet.util.scaleToString
import io.reactivex.Completable
import javax.inject.Inject

class DismissCardNotificationUseCase @Inject constructor(
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val preferences: SharedPreferencesReferralLocalData,
  private val autoUpdateRepository: AutoUpdateRepository,
  private val sharedPreferencesRepository: PreferencesRepositoryType,
  private val promotionsRepo: PromotionsRepository
) {

  operator fun invoke(cardNotification: CardNotification): Completable {
    return when (cardNotification) {
      is ReferralNotification -> referralDismiss(cardNotification)
      is UpdateNotification -> autoUpdateDismiss()
      is PromotionNotification -> promotionsDismiss(cardNotification.id)
      else -> Completable.complete()
    }
  }

  private fun referralDismiss(referralNotification: ReferralNotification): Completable {
    return findDefaultWalletUseCase()
      .flatMapCompletable {
        preferences.savePendingAmountNotification(
          it.address,
          referralNotification.pendingAmount.scaleToString(2)
        )
      }
  }

  private fun autoUpdateDismiss(): Completable {
    return autoUpdateRepository.loadAutoUpdateModel(false)
      .flatMapCompletable {
        sharedPreferencesRepository.saveAutoUpdateCardDismiss(it.updateVersionCode)
      }
  }

  private fun promotionsDismiss(id: String): Completable {
    return Completable.fromAction {
      promotionsRepo.setSeenGenericPromotion(id, PromotionUpdateScreen.TRANSACTIONS.name)
    }
  }
}