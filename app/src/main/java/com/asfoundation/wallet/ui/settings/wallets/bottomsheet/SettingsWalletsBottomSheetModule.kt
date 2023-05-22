package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.asfoundation.wallet.ui.settings.wallets.bottomsheet.SettingsWalletsBottomSheetFragment.Companion.WALLET_MODEL_KEY
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class SettingsWalletsBottomSheetModule {

  @Provides
  fun providesSettingsWalletsBottomSheetPresenter(fragment: Fragment,
                                                  navigator: SettingsWalletsBottomSheetNavigator,
                                                  walletsEventSender: WalletsEventSender,
                                                  data: SettingsWalletsBottomSheetData): SettingsWalletsBottomSheetPresenter {
    return SettingsWalletsBottomSheetPresenter(fragment as SettingsWalletsBottomSheetView,
        navigator, CompositeDisposable(), walletsEventSender, data)
  }

  @Provides
  fun providesSettingsWalletsBottomSheetData(
      fragment: Fragment): SettingsWalletsBottomSheetData {
    fragment.requireArguments()
        .apply {
          return SettingsWalletsBottomSheetData(getSerializable(WALLET_MODEL_KEY) as WalletsModel)
        }
  }
}