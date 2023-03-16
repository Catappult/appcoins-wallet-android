package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.interact.UpdateNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralNotification
import com.asfoundation.wallet.repository.AutoUpdateRepository
import com.appcoins.wallet.core.utils.common.extensions.scaleToString
import io.reactivex.Completable
import com.appcoins.wallet.sharedpreferences.ReferralPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class DismissCardNotificationUseCase @Inject constructor(
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val preferences: ReferralPreferencesDataSource,
  private val autoUpdateRepository: AutoUpdateRepository,
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
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
        Completable.fromCallable {
          preferences.savePendingAmountNotification(
            it.address,
            referralNotification.pendingAmount.scaleToString(2)
          )
        }
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