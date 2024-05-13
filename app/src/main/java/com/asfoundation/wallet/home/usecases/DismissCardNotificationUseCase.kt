package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.asfoundation.wallet.interact.UpdateNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.repository.AutoUpdateRepository
import io.reactivex.Completable
import javax.inject.Inject

class DismissCardNotificationUseCase @Inject constructor(
  private val autoUpdateRepository: AutoUpdateRepository,
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
  private val promotionsRepo: PromotionsRepository
) {

  operator fun invoke(cardNotification: CardNotification): Completable {
    return when (cardNotification) {
      is UpdateNotification -> autoUpdateDismiss()
      is PromotionNotification -> promotionsDismiss(cardNotification.id)
      else -> Completable.complete()
    }
  }

  private fun autoUpdateDismiss(): Completable {
    return autoUpdateRepository.loadAutoUpdateModel(false)
      .flatMapCompletable {
        Completable.fromCallable {
          commonsPreferencesDataSource.saveAutoUpdateCardDismiss(it.updateVersionCode)
        }
      }
  }

  private fun promotionsDismiss(id: String): Completable {
    return Completable.fromAction {
      promotionsRepo.setSeenGenericPromotion(id, PromotionUpdateScreen.TRANSACTIONS.name)
    }
  }
}