package com.asfoundation.wallet.promotions

import android.content.ActivityNotFoundException
import com.appcoins.wallet.gamification.repository.entity.Status
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.GAMIFICATION_INFO
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.REFERRAL_ID
import com.asfoundation.wallet.promotions.PromotionsInteractor.Companion.VOUCHER_ID
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
    promotionsInteractor.setHasBeenInPromotionsScreen()
    handlePromotionClicks()
    handleRetryClick()
    handleBottomSheetVisibility()
    handleBackPress()
    handleVouchersButtonClick()
    handlePerksButtonClick()
    handlePageChanged()
  }

  fun onResume() {
    if (shouldRequestPromotions()) retrievePromotions()
  }

  private fun retrievePromotions() {
    disposables.add(promotionsInteractor.retrievePromotions()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSubscribe { view.showLoading() }
        .doOnSuccess { onPromotions(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun onPromotions(promotionsModel: PromotionsModel) {
    view.hideLoading()
    when {
      promotionsModel.error == Status.NO_NETWORK -> {
        viewState = ViewState.NO_NETWORK
        view.showNetworkErrorView()
      }
      promotionsModel.walletOrigin == WalletOrigin.UNKNOWN -> {
        viewState = ViewState.UNKNOWN
        view.showLockedPromotionsScreen()
      }
      else -> {
        if (hasPromotions(promotionsModel)) {
          viewState = ViewState.PROMOTIONS
          cachedBonus = getMaxBonus(promotionsModel)
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

  private fun hasPromotions(promotionsModel: PromotionsModel): Boolean {
    return promotionsModel.promotions.isNotEmpty() || promotionsModel.vouchers.isNotEmpty()
        || promotionsModel.perks.isNotEmpty()
  }

  private fun getMaxBonus(promotionsModel: PromotionsModel): Double {
    for (promotion in promotionsModel.promotions) {
      if (promotion is GamificationItem) return promotion.maxBonus
    }
    return 0.0
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
      GAMIFICATION_ID -> navigator.navigateToGamification(cachedBonus)
      GAMIFICATION_INFO -> view.showBottomSheet()
      REFERRAL_ID -> mapReferralClick(promotionClick.extras)
      VOUCHER_ID -> navigateToVouchers(promotionClick.extras!!)
      else -> mapPackagePerkClick(promotionClick.extras)
    }
  }

  private fun navigateToVouchers(extras: Map<String, Any>) {
    navigator.navigateToVoucherDetails(
        extras.getValue(PromotionsViewHolder.TITLE_NAME_EXTRA) as String,
        extras.getValue(PromotionsViewHolder.FEATURE_GRAPHIC_EXTRA) as String,
        extras.getValue(PromotionsViewHolder.ICON_EXTRA) as String,
        extras.getValue(PromotionsViewHolder.MAX_BONUS) as Double,
        extras.getValue(PromotionsViewHolder.PACKAGE_NAME_EXTRA) as String,
        extras.getValue(PromotionsViewHolder.HAS_APPCOINS_EXTRA) as Boolean)
  }

  private fun mapReferralClick(extras: Map<String, Any>?) {
    if (extras != null) {
      val link = extras[ReferralViewHolder.KEY_LINK]
      if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_DETAILS) {
        navigator.navigateToInviteFriends()
      } else if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_SHARE && link != null) {
        navigator.handleShare(link as String)
      }
    }
  }

  private fun mapPackagePerkClick(extras: Map<String, Any>?) {
    if (extras != null && extras[PromotionsViewHolder.DETAILS_URL_EXTRA] != null) {
      val detailsLink = extras[PromotionsViewHolder.DETAILS_URL_EXTRA]
      try {
        navigator.openDetailsLink(detailsLink as String)
      } catch (exception: ActivityNotFoundException) {
        exception.printStackTrace()
        view.showToast()
      }
    }
  }

  private fun handleVouchersButtonClick() {
    disposables.add(view.getVouchersRadioButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.checkVouchersRadioButton() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handlePerksButtonClick() {
    disposables.add(view.getPerksRadioButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.checkPerksRadioButton() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handlePageChanged() {
    disposables.add(view.pageChangedCallback()
        .observeOn(viewScheduler)
        .doOnNext { view.changeButtonState(it) }
        .subscribe({}, { it.printStackTrace() }))
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

  private fun shouldRequestPromotions(): Boolean {
    return viewState == ViewState.DEFAULT || viewState == ViewState.UNKNOWN
  }

  enum class ViewState {
    DEFAULT, UNKNOWN, NO_NETWORK, PROMOTIONS, NO_PROMOTIONS
  }
}
