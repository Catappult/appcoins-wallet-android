package com.asfoundation.wallet.rating.negative

import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingNavigator
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RatingNegativePresenter(private val view: RatingNegativeView,
                              private val interactor: RatingInteractor,
                              private val navigator: RatingNavigator,
                              private val analytics: RatingAnalytics,
                              private val disposables: CompositeDisposable,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler) {

  fun present() {
    handleSubmitClick()
    handleNoClick()
  }

  private fun handleSubmitClick() {
    disposables.add(
        view.submitClickEvent()
            .doOnNext { feedbackText ->
              if (feedbackText.isBlank()) {
                view.showEmptySuggestionsError()
              } else {
                view.setLoading()
              }
            }
            .observeOn(ioScheduler)
            .doOnNext { analytics.sendNegativeActionEvent("submit") }
            .filter { feedbackText -> feedbackText.isNotBlank() }
            .flatMap { feedbackText ->
              interactor.sendUserFeedback(feedbackText)
                  .doOnComplete { navigator.navigateToFinish() }
                  .andThen(Observable.just(feedbackText))
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNoClick() {
    disposables.add(
        view.noClickEvent()
            .observeOn(ioScheduler)
            .doOnNext { analytics.sendNegativeActionEvent("no_thanks") }
            .observeOn(viewScheduler)
            .doOnNext { navigator.closeActivity() }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}
