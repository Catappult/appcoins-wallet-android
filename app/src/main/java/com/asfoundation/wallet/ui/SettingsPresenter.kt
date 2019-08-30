package com.asfoundation.wallet.ui

import android.preference.PreferenceManager
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
                        private val smsValidationInteract: SmsValidationInteract) {


  fun present() {
    view.setupPreferences()
    handleVerifyWalletPreferenceSummary()
    handleWalletsPreferenceSummary()
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
          PreferenceManager.getDefaultSharedPreferences(view.getContext())
              .edit()
              .putString("pref_wallet", wallet.address)
              .apply()
          view.setWalletsPreference(wallet.address)
        }, {}))
  }
}

