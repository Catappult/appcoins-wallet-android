package com.asfoundation.wallet.ui

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SettingsPresenter(private val view: SettingsView,
                        private val networkScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val disposables: CompositeDisposable,
                        private val findDefaultWalletInteract: FindDefaultWalletInteract,
                        private val smsValidationInteract: SmsValidationInteract,
                        private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun present() {
    view.setupPreferences()
    handleVerifyWalletPreferenceSummary()
    handleRedeemPreferenceSetup()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun handleVerifyWalletPreferenceSummary() {
    disposables.add(findDefaultWalletInteract.find()
        .flatMap { wallet ->
          smsValidationInteract.isValid(Wallet(wallet.address))
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .doOnSuccess {
                when (it) {
                  WalletValidationStatus.SUCCESS -> view.setVerifiedWalletPreference()
                  WalletValidationStatus.GENERIC_ERROR -> view.setUnverifiedWalletPreference()
                  else -> handleValidationCache(wallet)
                }
              }
        }
        .subscribe())
  }

  private fun handleValidationCache(wallet: Wallet) {
    val isVerified = preferencesRepositoryType.isWalletValidated(wallet.address)
    if (isVerified) {
      view.setVerifiedWalletPreference()
    } else {
      view.setWalletValidationNoNetwork()
    }
  }

  private fun handleRedeemPreferenceSetup() {
    disposables.add(findDefaultWalletInteract.find()
        .subscribe { wallet ->
          view.setRedeemCodePreference(wallet.address)
        })
  }

}

