package com.asfoundation.wallet.promotions

import android.content.ActivityNotFoundException
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.REFERRAL_ID
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class PromotionsPresenter(private val view: PromotionsView,
                          private val navigator: PromotionsNavigator,
                          private val promotionsInteractor: PromotionsInteractor,
                          private val disposables: CompositeDisposable,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler) {

  private var cachedBonus = 0.0
  private var viewState = ViewState.DEFAULT

  fun present() {
    handlePromotionClicks()
    handleGamificationInfoClicks()
    handleRetryClick()
    handleBottomSheetVisibility()
    handleBackPress()
  }

  fun onResume() {
    if (shouldRequestPromotions()) retrievePromotions()
  }

  private fun retrievePromotions() {
    disposables.add(promotionsInteractor.retrievePromotions()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSubscribe { view.showLoading() }
        .doOnNext { onPromotions(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun onPromotions(promotionsModel: PromotionsModel) {
    when {
      promotionsModel.hasError() && promotionsModel.fromCache -> {
        // not meant to display anything
        viewState = ViewState.DEFAULT
      }
      promotionsModel.error == Status.NO_NETWORK -> {
        if (shouldShowNoNetworkView()) {
          view.hideLoading()
          view.showNetworkErrorView()
          viewState = ViewState.NO_NETWORK
        }
      }
      promotionsModel.walletOrigin == WalletOrigin.UNKNOWN -> {
        // Locked Promotions is only shown after one makes sure user is still unknown
        // this is to avoid eventual flickering between view states (unknown and not unknown)
        if (!promotionsModel.fromCache) {
          view.hideLoading()
          viewState = ViewState.UNKNOWN
          view.showLockedPromotionsScreen()
        }
      }
      else -> {
        view.hideLoading()
        if (promotionsModel.promotions.isNotEmpty()) {
          viewState = ViewState.PROMOTIONS
          cachedBonus = promotionsModel.maxBonus
          view.showPromotions(promotionsModel)
          if (promotionsInteractor.shouldShowGamificationDisclaimer()) {
            view.showBottomSheet()
            promotionsInteractor.setGamificationDisclaimerShown()
          }
        } else {
          viewState = ViewState.NO_PROMOTIONS
          view.showNoPromotionsScreen()
        }
      }
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

  private fun handleGamificationInfoClicks() {
    disposables.add(view.getGamificationInfoClicks()
        .observeOn(viewScheduler)
        .doOnNext { view.showBottomSheet() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handlePromotionClicks() {
    disposables.add(view.getPromotionClicks()
        .observeOn(viewScheduler)
        .doOnNext { mapClickType(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun mapClickType(promotionClick: PromotionClick) {
    when (promotionClick.id) {
      GAMIFICATION_ID -> navigator.navigateToGamification(cachedBonus)
    }
  }

  private fun mapReferralClick(extras: Map<String, String>?) {
    if (extras != null) {
      val link = extras[ReferralViewHolder.KEY_LINK]
      if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_DETAILS) {
        navigator.navigateToInviteFriends()
      } else if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_SHARE && link != null) {
        navigator.handleShare(link)
      }
    }
  }

  private fun mapPackagePerkClick(extras: Map<String, String>?) {
    if (extras != null && extras[PromotionsViewHolder.DETAILS_URL_EXTRA] != null) {
      val detailsLink = extras[PromotionsViewHolder.DETAILS_URL_EXTRA]
      try {
        navigator.openDetailsLink(detailsLink!!)
      } catch (exception: ActivityNotFoundException) {
        exception.printStackTrace()
        view.showToast()
      }
    }
  }

  fun stop() = disposables.clear()

  private fun handleBottomSheetVisibility() {
    disposables.add(view.getBottomSheetButtonClick()
        .mergeWith(view.getBottomSheetContainerClick())
        .observeOn(viewScheduler)
        .doOnNext { view.hideBottomSheet() }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleBackPress() {
    disposables.add(view.getBackPressed()
        .observeOn(viewScheduler)
        .doOnNext { view.handleBackPressed() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun shouldRequestPromotions(): Boolean =
      viewState == ViewState.DEFAULT || viewState == ViewState.UNKNOWN

  private fun shouldShowNoNetworkView(): Boolean =
      viewState == ViewState.DEFAULT || viewState == ViewState.NO_NETWORK

  enum class ViewState {
    DEFAULT, UNKNOWN, NO_NETWORK, PROMOTIONS, NO_PROMOTIONS
  }
}
