package com.asfoundation.wallet.rating.positive

import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingNavigator
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RatingPositivePresenter(private val view: RatingPositiveView,
                              private val navigator: RatingNavigator,
                              private val interactor: RatingInteractor,
                              private val analytics: RatingAnalytics,
                              private val disposables: CompositeDisposable,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleRateAppClick()
    handleRemindMeLaterClick()
    handleNoClick()
  }

  private fun initializeView() {
    interactor.setImpression()
    view.initializeView(interactor.isNotFirstTime())
  }

  private fun handleRateAppClick() {
    disposables.add(
        view.rateAppClickEvent()
            .observeOn(ioScheduler)
            .doOnNext {
              analytics.sendPositiveActionEvent("rate", !interactor.isNotFirstTime())
            }
            .observeOn(viewScheduler)
            .doOnNext {
              navigator.navigateToRate()
              navigator.closeActivity()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleRemindMeLaterClick() {
    disposables.add(
        view.remindMeLaterClickEvent()
            .observeOn(ioScheduler)
            .doOnNext {
              analytics.sendPositiveActionEvent("remind_later", !interactor.isNotFirstTime())
              interactor.setRemindMeLater()
            }
            .observeOn(viewScheduler)
            .doOnNext { navigator.closeActivity() }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNoClick() {
    disposables.add(
        view.noClickEvent()
            .observeOn(ioScheduler)
            .doOnNext {
              analytics.sendPositiveActionEvent("no_thanks", !interactor.isNotFirstTime())
            }
            .observeOn(viewScheduler)
            .doOnNext { navigator.closeActivity() }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}
