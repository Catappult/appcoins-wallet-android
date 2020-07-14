package com.asfoundation.wallet.ui

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single

class SettingsInteract(private val findDefaultWalletInteract: FindDefaultWalletInteract,
                       private val smsValidationInteract: SmsValidationInteract,
                       private val preferencesRepositoryType: PreferencesRepositoryType,
                       private val supportInteractor: SupportInteractor,
                       private val walletsInteract: WalletsInteract,
                       private val autoUpdateInteract: AutoUpdateInteract,
                       private val walletsEventSender: WalletsEventSender) {

  fun isWalletValid(): Single<Pair<String, WalletValidationStatus>> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          smsValidationInteract.getValidationStatus(wallet.address)
              .map { Pair(wallet.address, it) }
        }
  }

  fun isWalletValidated(address: String) = preferencesRepositoryType.isWalletValidated(address)

  fun findWallet() = findDefaultWalletInteract.find()
      .map { it.address }

  fun retrieveWallets() = walletsInteract.retrieveWalletsModel()

  fun sendCreateSuccessEvent() {
    walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
        WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_SUCCESS)
  }

  fun sendCreateErrorEvent() {
    walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
        WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL)
  }

  fun displaySupportScreen() = supportInteractor.displayChatScreen()

  fun retriveUpdateIntent() = autoUpdateInteract.buildUpdateIntent()
}
