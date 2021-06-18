package com.asfoundation.wallet.promotions.ui

import android.content.ActivityNotFoundException
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.promotions.PromotionsViewHolder
import com.asfoundation.wallet.promotions.ReferralViewHolder
import com.asfoundation.wallet.promotions.model.PromotionClick
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import io.reactivex.Scheduler

sealed class PromotionsSideEffect : SideEffect {
  data class NavigateToGamification(val cachedBonus: Double) : PromotionsSideEffect()
  data class NavigateToDetailsLink(val detailsLink: String) : PromotionsSideEffect()
  data class NavigateToShare(val url: String) : PromotionsSideEffect()
  object NavigateToInviteFriends : PromotionsSideEffect()
  object NavigateToInfo : PromotionsSideEffect()
  object ShowErrorToast : PromotionsSideEffect()
}

data class PromotionsState(val promotionsModelAsync: Async<PromotionsModel> = Async.Uninitialized) :
    ViewState

class PromotionsViewModel(private val getPromotions: GetPromotionsUseCase,
                          private val analyticsSetup: AnalyticsSetup,
                          private val setSeenPromotions: SetSeenPromotionsUseCase,
                          private val setSeenWalletOrigin: SetSeenWalletOriginUseCase,
                          private val networkScheduler: Scheduler) :
    BaseViewModel<PromotionsState, PromotionsSideEffect>(initialState()) {

  companion object {
    fun initialState(): PromotionsState {
      return PromotionsState()
    }
  }

  fun fetchPromotions() {
    getPromotions()
        .subscribeOn(networkScheduler)
        .asAsyncToState(PromotionsState::promotionsModelAsync) {
          copy(promotionsModelAsync = it)
        }
        .doOnNext { promotionsModel ->
          if (promotionsModel.error == null) {
            analyticsSetup.setWalletOrigin(promotionsModel.walletOrigin)
            setSeenWalletOrigin(promotionsModel.wallet.address, promotionsModel.walletOrigin.name)
            setSeenPromotions(promotionsModel.promotions, promotionsModel.wallet.address)
          }
        }
        .repeatableScopedSubscribe(PromotionsState::promotionsModelAsync.name) { e ->
          e.printStackTrace()
        }
  }

  fun gamificationInfoClicked() {
    sendSideEffect { PromotionsSideEffect.NavigateToInfo }
  }

  fun promotionClicked(promotionClick: PromotionClick) {
    when (promotionClick.id) {
      PromotionsInteractor.GAMIFICATION_ID -> sendSideEffect {
        PromotionsSideEffect.NavigateToGamification(promotionsModelAsync.value?.maxBonus ?: 0.00)
      }
      PromotionsInteractor.REFERRAL_ID -> handleReferralClick(promotionClick.extras)
      else -> handlePackagePerkClick(promotionClick.extras)
    }
  }

  private fun handleReferralClick(extras: Map<String, String>?) {
    if (extras != null) {
      val link = extras[ReferralViewHolder.KEY_LINK]
      if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_DETAILS) {
        sendSideEffect { PromotionsSideEffect.NavigateToInviteFriends }
      } else if (extras[ReferralViewHolder.KEY_ACTION] == ReferralViewHolder.ACTION_SHARE && link != null) {
        sendSideEffect { PromotionsSideEffect.NavigateToShare(link) }
      }
    }
  }

  private fun handlePackagePerkClick(extras: Map<String, String>?) {
    if (extras != null && extras[PromotionsViewHolder.DETAILS_URL_EXTRA] != null) {
      val detailsLink = extras[PromotionsViewHolder.DETAILS_URL_EXTRA]
      try {
        sendSideEffect { PromotionsSideEffect.NavigateToDetailsLink(detailsLink!!) }
      } catch (exception: ActivityNotFoundException) {
        exception.printStackTrace()
        sendSideEffect { PromotionsSideEffect.ShowErrorToast }
      }
    }
  }
}