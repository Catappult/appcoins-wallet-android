package com.asfoundation.wallet.referrals

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class InviteFriendsFragmentPresenter(private val view: InviteFriendsFragmentView,
                                     private val referralInteractor: ReferralInteractorContract,
                                     private val disposable: CompositeDisposable,
                                     private val viewScheduler: Scheduler,
                                     private val networkScheduler: Scheduler) {

  fun present() {
    retrieveReferral()
    handleShareClicks()
    handleAppsGamesClicks()
  }

  private fun retrieveReferral() {
    disposable.add(referralInteractor.retrieveReferral()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.showNotificationCard(it.pendingAmount)
          view.setTextValues(it.amount, it.pendingAmount, it.currency)
        }.subscribe({}, { it.printStackTrace() }))
  }

  private fun handleShareClicks() {
    disposable.add(view.shareLinkClick()
        .observeOn(networkScheduler)
        .flatMapSingle { referralInteractor.retrieveReferral() }
        .doOnNext { view.showShare(it.link!!) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAppsGamesClicks() {
    disposable.add(view.appsAndGamesButtonClick()
        .doOnNext { view.navigateToAptoide() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}