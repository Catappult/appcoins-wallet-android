package com.asfoundation.wallet.referrals

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class InviteFriendsFragmentPresenter(private val view: InviteFriendsFragmentView,
                                     private val referralInteractor: ReferralInteractorContract,
                                     private val disposable: CompositeDisposable,
                                     private val viewScheduler: Scheduler,
                                     private val networkScheduler: Scheduler) {

  fun present() {
    handleTextValues()
    handleShareClicks()
    handleAppsGamesClicks()
  }

  private fun handleShareClicks() {
    disposable.add(view.shareLinkClick()
        .doOnNext { view.showShare() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleTextValues() {
    disposable.add(Single.zip(referralInteractor.getSingleReferralBonus(),
        referralInteractor.getPendingBonus(),
        BiFunction { referralBonus: String, pendingBonus: String ->
          Pair(referralBonus, pendingBonus)
        }).subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setTextValues(it.first, it.second) }
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