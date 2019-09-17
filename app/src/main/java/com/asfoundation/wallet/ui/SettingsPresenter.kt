package com.asfoundation.wallet.ui

import android.content.SharedPreferences
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SettingsPresenter(private val view: SettingsView,
                        private val networkScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val disposables: CompositeDisposable,
                        private val findDefaultWalletInteract: FindDefaultWalletInteract,
                        private val smsValidationInteract: SmsValidationInteract,
                        private val defaultSharedPreferences: SharedPreferences) {


  fun present() {
    view.setupPreferences()
    handleVerifyWalletPreferenceSummary()
    handleWalletsPreferenceSummary()
    handleRedeemPreferenceSetup()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun handleVerifyWalletPreferenceSummary() {
    disposables.add(findDefaultWalletInteract.find()
        .flatMap { smsValidationInteract.isValid(Wallet(it.address)) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it == WalletValidationStatus.SUCCESS) {
            view.setVerifiedWalletPreference()
          } else {
            view.setUnverifiedWalletPreference()
          }
        }
        .subscribe())
  }

  private fun handleWalletsPreferenceSummary() {
    disposables.add(findDefaultWalletInteract.find()
        .subscribe({ wallet ->
          addWalletPreference(wallet.address)
          view.setWalletsPreference(wallet.address)
        }, {}))
  }

  private fun addWalletPreference(address: String?) {
    defaultSharedPreferences
        .edit()
        .putString("pref_wallet", address)
        .apply()
  }

  private fun handleRedeemPreferenceSetup() {
    disposables.add(findDefaultWalletInteract.find()
        .subscribe { wallet ->
          view.setRedeemCodePreference(wallet.address)
        })
  }
}

