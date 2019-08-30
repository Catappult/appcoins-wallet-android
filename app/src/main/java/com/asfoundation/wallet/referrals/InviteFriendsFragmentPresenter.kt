package com.asfoundation.wallet.referrals

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class InviteFriendsFragmentPresenter(private val view: InviteFriendsFragmentView,
                                     private val activity: InviteFriendsActivityView?,
                                     private val referralInteractor: ReferralInteractorContract,
                                     private val disposable: CompositeDisposable,
                                     private val viewScheduler: Scheduler,
                                     private val networkScheduler: Scheduler) {

  fun present() {
    retrieveReferral()
    handleInfoButtonClick()
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
        }.flatMapCompletable {
          referralInteractor.saveReferralInformation(it.completed, it.receivedAmount.toString(),
              true, ReferralsScreen.INVITE_FRIENDS)
        }
        .subscribe({}, { it.printStackTrace() }))
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

  private fun handleInfoButtonClick() {
    activity?.let {
      disposable.add(it.getInfoButtonClick().doOnNext {
        view.changeBottomSheetState()
      }.subscribe())
    }
  }

  fun stop() {
    disposable.clear()
  }
}