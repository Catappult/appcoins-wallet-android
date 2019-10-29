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
    val isVerified =
        defaultSharedPreferences.getBoolean(WALLET_VERIFIED + wallet.address, false)
    if (isVerified) {
      view.setVerifiedWalletPreference()
    } else {
      view.setWalletValidationNoNetwork()
    }
  }

  private fun handleWalletsPreferenceSummary() {
    disposables.add(findDefaultWalletInteract.find()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe({ wallet ->
          addWalletPreference(wallet.address)
          view.setWalletsPreference(wallet.address)
        }, {}))
  }

  private fun addWalletPreference(address: String?) {
    defaultSharedPreferences
        .edit()
        .putString(PREF_WALLET, address)
        .apply()
  }

  private fun handleRedeemPreferenceSetup() {
    disposables.add(findDefaultWalletInteract.find()
        .subscribe { wallet ->
          view.setRedeemCodePreference(wallet.address)
        })
  }

  private companion object {
    private const val PREF_WALLET = "pref_wallet"
    private const val WALLET_VERIFIED = "wallet_verified_"
  }
}

