package com.asfoundation.wallet.promo_code.bottom_sheet.entry

import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.promocode.data.PromoCodeResult
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.use_cases.DeletePromoCodeUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.GetUpdatedPromoCodeUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.VerifyAndSavePromoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import javax.inject.Inject

sealed class PromoCodeBottomSheetSideEffect : SideEffect {
  object NavigateBack : PromoCodeBottomSheetSideEffect()
}

data class PromoCodeBottomSheetState(
  val deeplinkPromoCode: Async<String> = Async.Uninitialized,
  val storedPromoCodeAsync: Async<PromoCode> = Async.Uninitialized,
  val submitPromoCodeAsync: Async<PromoCodeResult> = Async.Uninitialized,
  var shouldShowDefault: Boolean = false
) : ViewState

@HiltViewModel
class PromoCodeBottomSheetViewModel @Inject constructor(
  private val getUpdatedPromoCodeUseCase: GetUpdatedPromoCodeUseCase,
  private val verifyAndSavePromoCodeUseCase: VerifyAndSavePromoCodeUseCase,
  private val deletePromoCodeUseCase: DeletePromoCodeUseCase
) :
  BaseViewModel<PromoCodeBottomSheetState, PromoCodeBottomSheetSideEffect>(initialState()) {

  // to prevent success being called multiple times when any async is changed
  var isFirstSuccess = true

  companion object {
    fun initialState(): PromoCodeBottomSheetState {
      return PromoCodeBottomSheetState()
    }
  }

  fun initialize(deeplinkPromoCode: String?) {
    deeplinkPromoCode?.let {
      Single.just(it)
        .asAsyncToState { copy(deeplinkPromoCode = it) }
        .scopedSubscribe()
    } ?: getUpdatedPromoCodeUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = it) }
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