package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.repository.entity.Status
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.REFERRAL_ID
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class PromotionsPresenter(
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
          view.hideNetworkErrorView()
          view.showLoading()
        }
        .doOnSuccess { onPromotions(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun onPromotions(promotionsModel: PromotionsModel) {
    view.hideLoading()
    when {
      promotionsModel.error == Status.NO_NETWORK -> view.showNetworkErrorView()
      promotionsModel.promotions.isNotEmpty() -> {
        cachedBonus = promotionsModel.maxBonus
        view.showPromotions(promotionsModel)
      }
      else -> view.showNoPromotionsScreen()
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
        .observeOn(viewScheduler)
        .doOnNext { retrievePromotions() }
        .subscribe({}, { handleError(it) }))
  }

  private fun handlePromotionClicks() {
    disposables.add(view.getPromotionClicks()
        .observeOn(viewScheduler)
        .doOnNext { mapClickType(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun mapClickType(promotionClick: PromotionClick) {
    when (promotionClick.id) {
      GAMIFICATION_ID -> activityView.navigateToGamification(cachedBonus)
      REFERRAL_ID -> mapReferralClick(promotionClick.extras)
      else -> mapPackagePerkClick(promotionClick.extras)
    }
  }

  private fun mapReferralClick(extras: Map<String, String>?) {
    if (extras != null) {
      val link = extras[ReferralViewHolder.KEY_LINK]
      if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_DETAILS) {
        activityView.navigateToInviteFriends()
      } else if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_SHARE && link != null) {
        activityView.handleShare(link)
      }
    }
  }

  private fun mapPackagePerkClick(extras: Map<String, String>?) {
    if (extras != null && extras[PromotionsViewHolder.APP_NAME_EXTRA] != null) {
      val appName = extras[PromotionsViewHolder.APP_NAME_EXTRA]
      activityView.openAppView("https://$appName.en.aptoide.com/app")
    }
  }

  fun stop() = disposables.clear()

}
