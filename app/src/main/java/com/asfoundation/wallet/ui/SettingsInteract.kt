package com.asfoundation.wallet.ui

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single

class SettingsInteract(private val findDefaultWalletInteract: FindDefaultWalletInteract,
                       private val smsValidationInteract: SmsValidationInteract,
                       private val preferencesRepositoryType: PreferencesRepositoryType,
                       private val walletsInteract: WalletsInteract) {

  fun isWalletValid(): Single<Pair<String, WalletValidationStatus>> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          smsValidationInteract.isValid(wallet.address)
              .map { Pair(wallet.address, it) }
        }
  }

  fun isWalletValidated(address: String) = preferencesRepositoryType.isWalletValidated(address)

  fun findWallet() = findDefaultWalletInteract.find()
      .map { it.address }

  fun retrieveWallets() = walletsInteract.retrieveWalletsModel()
}
