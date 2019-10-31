package com.asfoundation.wallet.interact

import com.asf.wallet.R
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralNotification
import com.asfoundation.wallet.repository.SharedPreferenceRepository
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class CardNotificationsInteractor(private val referralInteractor: ReferralInteractorContract,
                                  private val autoUpdateInteract: AutoUpdateInteract,
                                  private val sharedPreferenceRepository: SharedPreferenceRepository) {


  fun getCardNotifications(): Single<List<CardNotification>> {
    return Single.zip(referralInteractor.getUnwatchedPendingBonusNotification(),
        getUnwatchedUpdateNotification(),
        BiFunction { referralNotification: CardNotification, updateNotification: CardNotification ->
          val list = ArrayList<CardNotification>()
          if (referralNotification !is EmptyNotification) list.add(referralNotification)
          if (updateNotification !is EmptyNotification) list.add(updateNotification)
          list
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

  private fun getUnwatchedUpdateNotification(): Single<CardNotification> {
    return autoUpdateInteract.getAutoUpdateModel(false)
        .flatMap { updateModel ->
          sharedPreferenceRepository.getAutoUpdateCardDismissedVersion()
              .map {
                autoUpdateInteract.hasSoftUpdate(updateModel.updateVersionCode,
                    updateModel.updateMinSdk) && updateModel.updateVersionCode != it
              }
        }
        .map { shouldShow ->
          UpdateNotification(AUTO_UPDATE_ID,
              R.string.test_title,
              R.string.test_description,
              R.string.test_button, CardNotificationAction.UPDATE,
              R.raw.update_animation).takeIf { shouldShow } ?: EmptyNotification()
        }
  }

  companion object {
    const val AUTO_UPDATE_ID = 2
  }
}