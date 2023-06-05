package com.asfoundation.wallet.promo_code.bottom_sheet.entry

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.promocode.data.FailedPromoCode
import com.appcoins.wallet.feature.promocode.data.PromoCodeResult
import com.appcoins.wallet.feature.promocode.data.use_cases.DeletePromoCodeUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.GetStoredPromoCodeResultUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.VerifyAndSavePromoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class PromoCodeBottomSheetSideEffect : SideEffect {
  object NavigateBack : PromoCodeBottomSheetSideEffect()
}

data class PromoCodeBottomSheetState(
    val storedPromoCodeAsync: Async<com.appcoins.wallet.feature.promocode.data.PromoCodeResult> = Async.Uninitialized,
    val submitPromoCodeAsync: Async<com.appcoins.wallet.feature.promocode.data.PromoCodeResult> = Async.Uninitialized,
    val shouldShowDefault: Boolean = false
) : ViewState

@HiltViewModel
class PromoCodeBottomSheetViewModel @Inject constructor(
    private val getStoredPromoCodeResultUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetStoredPromoCodeResultUseCase,
    private val verifyAndSavePromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.VerifyAndSavePromoCodeUseCase,
    private val deletePromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.DeletePromoCodeUseCase
) :
  BaseViewModel<PromoCodeBottomSheetState, PromoCodeBottomSheetSideEffect>(initialState()) {

  // to prevent success being called multiple times when any async is changed
  var isFirstSuccess = true

  companion object {
    fun initialState(): PromoCodeBottomSheetState {
      return PromoCodeBottomSheetState()
    }
  }

  init {
    getCurrentPromoCode()
  }

  private fun getCurrentPromoCode() {
    getStoredPromoCodeResultUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = it) }
      .doOnNext {
        if (it == com.appcoins.wallet.feature.promocode.data.FailedPromoCode.ExpiredCode()) deleteCode()
      }
      .repeatableScopedSubscribe(PromoCodeBottomSheetState::storedPromoCodeAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun submitClick(promoCodeString: String) {
    verifyAndSavePromoCodeUseCase(promoCodeString)
      .asAsyncToState { copy(submitPromoCodeAsync = it) }
      .scopedSubscribe()
  }

  fun replaceClick() = setState { copy(shouldShowDefault = true) }

  fun deleteClick() {
    deletePromoCodeUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = Async.Uninitialized) }
      .doOnComplete {
        sendSideEffect {
          PromoCodeBottomSheetSideEffect.NavigateBack
        }
      }
      .scopedSubscribe()
  }

  private fun deleteCode() {
    deletePromoCodeUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = Async.Uninitialized) }
      .scopedSubscribe()
  }
}