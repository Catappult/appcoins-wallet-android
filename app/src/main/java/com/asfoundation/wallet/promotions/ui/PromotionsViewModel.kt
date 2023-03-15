package com.asfoundation.wallet.promotions.ui


import android.content.ActivityNotFoundException
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.core.analytics.analytics.AnalyticsSetup
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.model.VipReferralInfo
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class PromotionsSideEffect : SideEffect {
  data class NavigateToGamification(val cachedBonus: Double) : PromotionsSideEffect()
  data class NavigateToVoucherDetails(val packageName: String) : PromotionsSideEffect()
  data class NavigateToOpenDetails(val link: String) : PromotionsSideEffect()
  data class NavigateToShare(val url: String) : PromotionsSideEffect()
  data class NavigateToVipReferral(
    val bonus: String,
    val promoCodeVip: String,
    val totalEarned: String,
    val numberReferrals: String
  ) : PromotionsSideEffect()
  object NavigateToInviteFriends : PromotionsSideEffect()
  object NavigateToInfo : PromotionsSideEffect()
  object ShowErrorToast : PromotionsSideEffect()
}

data class PromotionsState(
  val promotionsModelAsync: Async<PromotionsModel> = Async.Uninitialized,
  val promotionsGamificationStatsAsync: Async<PromotionsGamificationStats> = Async.Uninitialized
) :
  ViewState

@HiltViewModel
class PromotionsViewModel @Inject constructor(
    private val getPromotions: GetPromotionsUseCase,
    private val analyticsSetup: AnalyticsSetup,
    private val setSeenPromotions: SetSeenPromotionsUseCase,
    private val setSeenWalletOrigin: SetSeenWalletOriginUseCase,
    private val gamificationInteractor: GamificationInteractor,
    private val rxSchedulers: RxSchedulers
) :
  BaseViewModel<PromotionsState, PromotionsSideEffect>(initialState()) {

  var vipReferral: VipReferralInfo? = null

  companion object {
    const val DETAILS_URL_EXTRA = "DETAILS_URL_EXTRA"
    const val PACKAGE_NAME_EXTRA = "PACKAGE_NAME_EXTRA"

    const val KEY_ACTION = "ACTION"
    const val KEY_LINK = "LINK"
    const val ACTION_DETAILS = "DETAILS"
    const val ACTION_SHARE = "SHARE"

    fun initialState(): PromotionsState {
      return PromotionsState()
    }
  }

  fun fetchPromotions() {
    getPromotions()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState(PromotionsState::promotionsModelAsync) {
        copy(promotionsModelAsync = it)
      }
      .doOnNext { promotionsModel ->
        if (promotionsModel.error == null) {
          analyticsSetup.setWalletOrigin(promotionsModel.walletOrigin.name)
          setSeenWalletOrigin(promotionsModel.wallet.address, promotionsModel.walletOrigin.name)
          setSeenPromotions(promotionsModel.promotions, promotionsModel.wallet.address)
        }
      }
      .repeatableScopedSubscribe(PromotionsState::promotionsModelAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun fetchGamificationStats() {
    gamificationInteractor.getUserStats()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState { copy(promotionsGamificationStatsAsync = it) }
      .scopedSubscribe()
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
      PromotionsInteractor.VOUCHER_ID -> sendSideEffect {
        PromotionsSideEffect.NavigateToVoucherDetails(
          promotionClick.extras!!.getValue(PACKAGE_NAME_EXTRA)
        )
      }
      else -> mapPackagePerkClick(promotionClick.extras)
    }
  }

  fun vipReferralClicked() {
    val vipReferral = state.promotionsModelAsync()?.vipReferralInfo
    vipReferral?.let {
      sendSideEffect {
        PromotionsSideEffect.NavigateToVipReferral(
          it.vipBonus,
          it.vipCode,
          it.totalEarned,
          it.numberReferrals
        )
      }
    }
  }

  private fun handleReferralClick(extras: Map<String, String>?) {
    if (extras != null) {
      val link = extras[KEY_LINK]
      if (extras[KEY_ACTION] == ACTION_DETAILS) {
        sendSideEffect { PromotionsSideEffect.NavigateToInviteFriends }
      } else if (extras[KEY_ACTION] == ACTION_SHARE && link != null) {
        sendSideEffect { PromotionsSideEffect.NavigateToShare(link) }
      }
    }
  }

  private fun mapPackagePerkClick(extras: Map<String, String>?) {
    if (extras != null && extras[DETAILS_URL_EXTRA] != null) {
      val detailsLink = extras[DETAILS_URL_EXTRA]
      try {
        sendSideEffect { PromotionsSideEffect.NavigateToOpenDetails(detailsLink!!) }
      } catch (exception: ActivityNotFoundException) {
        exception.printStackTrace()
      }
    }
  }
}