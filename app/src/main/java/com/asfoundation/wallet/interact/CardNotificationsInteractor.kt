package com.asfoundation.wallet.interact

import com.asf.wallet.R
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralNotification
import com.asfoundation.wallet.repository.SharedPreferenceRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction

class CardNotificationsInteractor(private val referralInteractor: ReferralInteractorContract,
                                  private val autoUpdateInteract: AutoUpdateInteract,
                                  private val sharedPreferenceRepository: SharedPreferenceRepository) {


  fun getCardNotifications(): Maybe<List<CardNotification>> {
    return Maybe.zip(referralInteractor.getUnwatchedPendingBonusNotification(),
        getUnwatchedUpdateNotification(),
        BiFunction { referralNotification: CardNotification, updateNotification: CardNotification ->
          listOf(referralNotification, updateNotification)
        })
  }

  fun dismissNotification(cardNotification: CardNotification): Completable {
    return if (cardNotification is ReferralNotification) {
      referralInteractor.dismissNotification(cardNotification)
    } else {
      autoUpdateInteract.getAutoUpdateModel(false)
          .flatMapCompletable {
            sharedPreferenceRepository.saveAutoUpdateCardDismiss(it.updateVersionCode)
          }
    }
  }

  private fun getUnwatchedUpdateNotification(): Maybe<CardNotification> {
    return autoUpdateInteract.getAutoUpdateModel(false)
        .flatMapMaybe { updateModel ->
          sharedPreferenceRepository.getAutoUpdateCardDismissedVersion()
              .filter {
                autoUpdateInteract.hasSoftUpdate(updateModel.updateVersionCode,
                    updateModel.updateMinSdk) && updateModel.updateVersionCode != it
              }
        }
        .map {
          UpdateNotification(AUTO_UPDATE_ID,
              R.string.test_title,
              R.string.test_description,
              R.raw.update_animation)
        }
  }

  companion object {
    const val AUTO_UPDATE_ID = 2
  }
}