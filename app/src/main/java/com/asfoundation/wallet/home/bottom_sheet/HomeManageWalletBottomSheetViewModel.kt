package com.asfoundation.wallet.home.bottom_sheet

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.NewBaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.ui.login.usecases.GenerateWebLoginUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class HomeManageWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : HomeManageWalletBottomSheetSideEffect()
  data class OpenLogin(val url: String, val launcher: ActivityResultLauncher<Intent>) : HomeManageWalletBottomSheetSideEffect()
}

data class HomeManageWalletBottomSheetState(
  val currentWalletAsync: Async<WalletInfo> = Async.Uninitialized
) : ViewState

@HiltViewModel
class HomeManageWalletBottomSheetViewModel
@Inject
constructor(
  private val walletsEventSender: WalletsEventSender,
  private val generateWebLoginUrlUseCase: GenerateWebLoginUrlUseCase,
) :
  NewBaseViewModel<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect>(
    initialState()
  ) {

  companion object {
    fun initialState(): HomeManageWalletBottomSheetState {
      return HomeManageWalletBottomSheetState()
    }
  }

  fun sendOpenBackupEvent() {
    walletsEventSender.sendCreateBackupEvent(
      WalletsAnalytics.ACTION_CREATE,
      WalletsAnalytics.CONTEXT_CARD,
      WalletsAnalytics.STATUS_SUCCESS
    )
  }

  fun getLoginUrl(): String {
    return generateWebLoginUrlUseCase()
      .doOnError { error -> Log.d("getLoginUrl", "Error: ${error.message}") }
      .blockingGet()
  }
}
