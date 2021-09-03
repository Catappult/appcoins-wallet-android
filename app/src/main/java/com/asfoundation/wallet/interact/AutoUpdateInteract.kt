package com.asfoundation.wallet.interact

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.repository.AutoUpdateRepository
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Completable
import io.reactivex.Single

class AutoUpdateInteract(private val autoUpdateRepository: AutoUpdateRepository,
                         private val walletVersionCode: Int, private val deviceSdk: Int,
                         private val packageManager: PackageManager,
                         private val walletPackageName: String,
                         private val sharedPreferencesRepository: PreferencesRepositoryType) {

  fun getAutoUpdateModel(invalidateCache: Boolean = true): Single<AutoUpdateModel> {
    return autoUpdateRepository.loadAutoUpdateModel(invalidateCache)
  }

  fun hasSoftUpdate(updateVersionCode: Int, updatedMinSdk: Int): Boolean {
    return walletVersionCode < updateVersionCode && deviceSdk >= updatedMinSdk
  }

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int,
                           updateMinSdk: Int): Boolean {
    return blackList.contains(walletVersionCode) && hasSoftUpdate(updateVersionCode, updateMinSdk)
  }

  fun retrieveRedirectUrl(): String {
    return String.format(PLAY_APP_VIEW_URL, walletPackageName)
  }

  fun buildUpdateIntent(): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(retrieveRedirectUrl()))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
  }

  fun getUnwatchedUpdateNotification(): Single<CardNotification> {
    return getAutoUpdateModel(false)
        .flatMap { updateModel ->
          sharedPreferencesRepository.getAutoUpdateCardDismissedVersion()
              .map {
                hasSoftUpdate(updateModel.updateVersionCode,
                    updateModel.updateMinSdk) && updateModel.updateVersionCode != it
              }
        }
        .map { shouldShow ->
          UpdateNotification(
              R.string.update_wallet_soft_title,
              R.string.update_wallet_soft_body,
              R.string.update_button, CardNotificationAction.UPDATE,
              R.raw.soft_hard_update_animation).takeIf { shouldShow } ?: EmptyNotification()
        }
  }

  private fun isAptoideInstalled(): Boolean {
    return try {
      packageManager.getApplicationInfo(APTOIDE_PACKAGE_NAME, 0)
          .enabled
    } catch (exception: PackageManager.NameNotFoundException) {
      false
    }
  }

  fun shouldShowNotification(): Boolean {
    val savedTime = sharedPreferencesRepository.getUpdateNotificationSeenTime()
    val currentTime = System.currentTimeMillis()
    val timeToShowNextNotificationInMillis = 3600000 * 12
    return currentTime >= savedTime + timeToShowNextNotificationInMillis
  }

  fun saveSeenUpdateNotification() =
      sharedPreferencesRepository.setUpdateNotificationSeenTime(System.currentTimeMillis())

  fun dismissNotification(): Completable {
    return getAutoUpdateModel(false)
        .flatMapCompletable {
          sharedPreferencesRepository.saveAutoUpdateCardDismiss(it.updateVersionCode)
        }
  }

  companion object {
    private const val APTOIDE_PACKAGE_NAME = "cm.aptoide.pt"
    private const val APTOIDE_APP_VIEW_URL = "aptoideinstall://package=%s&show_install_popup=false"
    const val PLAY_APP_VIEW_URL = "market://details?id=%s"
  }
}
