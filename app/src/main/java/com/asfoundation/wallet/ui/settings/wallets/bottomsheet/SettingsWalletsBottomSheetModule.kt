package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

/*
@InstallIn(FragmentComponent::class)
@Module
class SettingsWalletsBottomSheetModule {
//Commented because not in use
  //@Provides
  fun providesSettingsWalletsBottomSheetPresenter(fragment: Fragment,
                                                  navigator: SettingsWalletsBottomSheetNavigator,
                                                  walletsEventSender: WalletsEventSender,
                                                  data: SettingsWalletsBottomSheetData
  ): SettingsWalletsBottomSheetPresenter {
    return com.asfoundation.wallet.backup.entryBottomSheet.SettingsWalletsBottomSheetPresenter(
      fragment as SettingsWalletsBottomSheetView,
      navigator, CompositeDisposable(), walletsEventSender, data
    )
  }

  //@Provides
  fun providesSettingsWalletsBottomSheetData(
      fragment: Fragment): SettingsWalletsBottomSheetData {
    fragment.requireArguments()
        .apply {
          return SettingsWalletsBottomSheetData(getSerializable(WALLET_MODEL_KEY) as WalletsModel)
        }
  }
}

 */
