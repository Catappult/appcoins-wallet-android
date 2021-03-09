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
        viewState = ViewState.UNKNOWN_USER
        if (promotionsModel.vouchers.isNotEmpty()) {
          view.showLockedPromotionsWithVouchers(promotionsModel.vouchers)
        } else {
          view.showLockedPromotionsScreen()
        }
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
      REFERRAL_ID -> mapReferralClick(promotionClick)
      VOUCHER_ID -> navigateToVouchers(promotionClick)
      else -> mapPackagePerkClick(promotionClick)
    }
  }

  private fun navigateToVouchers(promotionClick: PromotionClick) {
    if (promotionClick is VoucherClick) {
      navigator.navigateToVoucherDetails(promotionClick.title, promotionClick.featureGraphic,
          promotionClick.icon, promotionClick.maxBonus, promotionClick.packageName,
          promotionClick.hasAppcoins)
    } else {
      view.showToast()
    }
  }

  private fun mapReferralClick(promotionClick: PromotionClick) {
    if (promotionClick is ReferralClick) {
      val link = promotionClick.link
      val action = promotionClick.action
      if (action == ReferralViewHolder.ACTION_DETAILS) {
        navigator.navigateToInviteFriends()
      } else if (action == ReferralViewHolder.ACTION_SHARE) {
        navigator.handleShare(link)
      }
    } else {
      view.showToast()
    }
  }

  private fun mapPackagePerkClick(promotionClick: PromotionClick) {
    if (promotionClick is AppPromotionClick) {
      try {
        navigator.openDetailsLink(promotionClick.downloadLink)
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

  fun stop() {
    viewState = ViewState.DEFAULT
    disposables.clear()
  }

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
    return viewState == ViewState.DEFAULT || viewState == ViewState.UNKNOWN_USER
  }

  enum class ViewState {
    DEFAULT, UNKNOWN_USER, NO_NETWORK, PROMOTIONS, NO_PROMOTIONS
  }
}
