package com.asfoundation.wallet.update_required.use_cases

import com.asf.wallet.R
import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.interact.UpdateNotification
import com.asfoundation.wallet.referrals.CardNotification
import repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import io.reactivex.Single
import javax.inject.Inject

class GetUnwatchedUpdateNotificationUseCase @Inject constructor(
  private val getAutoUpdateModelUseCase: GetAutoUpdateModelUseCase,
  private val hasSoftUpdateUseCase: HasSoftUpdateUseCase,
  private val sharedPreferencesRepository: PreferencesRepositoryType
) {

  operator fun invoke(): Single<CardNotification> {
    return getAutoUpdateModelUseCase(false)
      .flatMap { updateModel ->
        sharedPreferencesRepository.getAutoUpdateCardDismissedVersion()
          .map {
            hasSoftUpdateUseCase(
              updateModel.updateVersionCode,
              updateModel.updateMinSdk
            ) && updateModel.updateVersionCode != it
          }
      }
      .map { shouldShow ->
        UpdateNotification(
          R.string.update_wallet_soft_title,
          R.string.update_wallet_soft_body,
          R.string.update_button, CardNotificationAction.UPDATE,
          R.raw.soft_hard_update_animation
        ).takeIf { shouldShow } ?: EmptyNotification()
      }
  }
}