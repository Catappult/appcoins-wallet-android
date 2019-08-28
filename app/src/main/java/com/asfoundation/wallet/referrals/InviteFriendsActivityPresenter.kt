package com.asfoundation.wallet.referrals

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class InviteFriendsActivityPresenter(private val activity: InviteFriendsActivityView,
                                     private val smsValidationInteract: SmsValidationInteract,
                                     private val walletInteract: FindDefaultWalletInteract,
                                     private val disposables: CompositeDisposable,
                                     private val networkScheduler: Scheduler,
                                     private val viewScheduler: Scheduler) {

  fun present() {
    handleFragmentNavigation()
  }

  private fun handleFragmentNavigation() {
    disposables.add(walletInteract.find()
        .flatMap { smsValidationInteract.isValid(it) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleValidationResult(it) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleValidationResult(validationStatus: WalletValidationStatus) {
    when (validationStatus) {
      WalletValidationStatus.SUCCESS -> {
        activity.navigateToInviteFriends()
        handleInfoButtonVisibility()
      }
      WalletValidationStatus.NO_NETWORK -> activity.showNoNetworkScreen()
      else -> activity.navigateToVerificationFragment()
    }
  }

  private fun handleInfoButtonVisibility() {
    disposables.add(activity.infoButtonInitialized()
        .filter { it }
        .doOnNext { activity.showInfoButton() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }
}