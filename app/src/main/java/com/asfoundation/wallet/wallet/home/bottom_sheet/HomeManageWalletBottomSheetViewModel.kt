package com.asfoundation.wallet.wallet.home.bottom_sheet

import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.backup.ui.triggers.TriggerUtils.toJson
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel
import com.asfoundation.wallet.wallet.home.HomeSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class HomeManageWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : HomeManageWalletBottomSheetSideEffect()
}

data class HomeManageWalletBottomSheetState(
  val currentWalletAsync: Async<Wallet> = Async.Uninitialized
) : ViewState

@HiltViewModel
class HomeManageWalletBottomSheetViewModel @Inject constructor(
  private val walletsEventSender: WalletsEventSender,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,

  ) :
  BaseViewModel<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect>(
    initialState()
  ) {

  companion object {
    fun initialState(): HomeManageWalletBottomSheetState {
      return HomeManageWalletBottomSheetState()
    }
  }

  fun onBackupClick() {
    getCurrentWalletUseCase()
      .asAsyncToState {
        copy( currentWalletAsync = it)
      }.scopedSubscribe { e -> e.printStackTrace() }
  }

  fun sendOpenBackupEvent() {
    walletsEventSender.sendCreateBackupEvent(
      WalletsAnalytics.ACTION_CREATE,
      WalletsAnalytics.CONTEXT_CARD,
      WalletsAnalytics.STATUS_SUCCESS
    )
  }
}