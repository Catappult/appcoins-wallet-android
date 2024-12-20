package com.asfoundation.wallet.backup.entryBottomSheet

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.asfoundation.wallet.backup.entryBottomSheet.BackupEntryChooseWalletBottomSheetFragment.Companion.WALLET_MODEL_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class BackupEntryChooseWalletBottomSheetModule {

  @Provides
  fun providesSettingsWalletsBottomSheetPresenter(
    fragment: Fragment,
    navigator: BackupEntryChooseWalletBottomSheetNavigator,
    walletsEventSender: WalletsEventSender,
    data: BackupEntryChooseWalletBottomSheetData
  ): BackupEntryChooseWalletBottomSheetPresenter {
    return BackupEntryChooseWalletBottomSheetPresenter(
      fragment as BackupEntryChooseWalletBottomSheetView,
      navigator, CompositeDisposable(), walletsEventSender, data
    )
  }

  @Provides
  fun providesSettingsWalletsBottomSheetData(
    fragment: Fragment
  ) =
    BackupEntryChooseWalletBottomSheetData(
      fragment.getSerializableExtra<WalletsModel>(WALLET_MODEL_KEY)!!
    )
}