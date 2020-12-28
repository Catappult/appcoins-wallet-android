package com.asfoundation.wallet.rating.entry

import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingNavigator
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RatingEntryPresenter(private val view: RatingEntryView,
                           private val navigator: RatingNavigator,
                           private val interactor: RatingInteractor,
                           private val analytics: RatingAnalytics,
                           private val disposables: CompositeDisposable,
                           private val viewScheduler: Scheduler,
                           private val ioScheduler: Scheduler) {

  fun present() {
    interactor.setImpression()
    navigator.disableActivityBack()
    handleYesClick()
    handleNoClick()
    handleBackPressed()
  }

  private fun handleBackPressed() {
    disposables.add(
        navigator.onBackPressed()
            .observeOn(ioScheduler)
            .doOnNext { analytics.sendWelcomeActionEvent("impression") }
            .observeOn(viewScheduler)
            .doOnNext {
              interactor.setRemindMeLater()
              navigator.enableActivityBack()
              navigator.closeActivity()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleYesClick() {
    disposables.add(
        view.yesClickEvent()
            .observeOn(ioScheduler)
            .doOnNext { analytics.sendWelcomeActionEvent("yes") }
            .observeOn(viewScheduler)
            .doOnNext {
              navigator.enableActivityBack()
              navigator.navigateToThankYou()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNoClick() {
    disposables.add(
        view.noClickEvent()
            .observeOn(ioScheduler)
            .doOnNext { analytics.sendWelcomeActionEvent("not_really") }
            .observeOn(viewScheduler)
            .doOnNext {
              navigator.enableActivityBack()
              navigator.navigateToSuggestions()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}
