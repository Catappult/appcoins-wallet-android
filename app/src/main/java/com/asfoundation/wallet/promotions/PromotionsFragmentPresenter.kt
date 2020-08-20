package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class PromotionsFragmentPresenter(
    private val view: PromotionsView,
    private val activityView: PromotionsActivityView,
    private val promotionsInteractor: PromotionsInteractorContract,
    private val disposables: CompositeDisposable,
    private val networkScheduler: Scheduler,
    private val viewScheduler: Scheduler) {

  var cachedBonus = 0.0

  fun present() {
    retrievePromotions()
    handlePromotionClicks()
    handleRetryClick()
  }

  private fun retrievePromotions() {
    disposables.add(promotionsInteractor.retrievePromotions()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSubscribe {
          view.hidePromotions()
          view.showLoading()
        }
        .doOnSuccess { onPromotions(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun onPromotions(promotionsModel: PromotionsModel) {
    view.hideLoading()
    if (promotionsModel.promotions.isNotEmpty()) {
      cachedBonus = promotionsModel.maxBonus
      view.showPromotions(promotionsModel)
    } else {
      view.showNoPromotionsScreen()
    }
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.hideLoading()
    if (throwable.isNoNetworkException()) view.showNetworkErrorView()
  }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext { retrievePromotions() }
        .subscribe({}, { handleError(it) }))
  }

  private fun handlePromotionClicks() {
    disposables.add(view.getPromotionClicks()
        .observeOn(viewScheduler)
        .doOnNext { mapClickType(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun mapClickType(id: String) {
    if (id == GAMIFICATION_ID) {
      activityView.navigateToGamification(cachedBonus)
    }
  }

  fun stop() = disposables.clear()

}
