package com.asfoundation.wallet.ui.settings.entry

import androidx.fragment.app.Fragment
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import com.asfoundation.wallet.ui.webview_login.usecases.GenerateWebLoginUrlUseCase
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class SettingsModule {
  @Provides
  fun providesSettingsPresenter(
    settingsFragment: Fragment,
    navigator: SettingsNavigator,
    interactor: SettingsInteractor,
    data: SettingsData,
    buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
    getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
    displayChatUseCase: DisplayChatUseCase,
    getStoredCardsUseCase: GetStoredCardsUseCase,
    generateWebLoginUrlUseCase: GenerateWebLoginUrlUseCase
  ): SettingsPresenter {
    return SettingsPresenter(
      settingsFragment as SettingsView,
      navigator,
      Schedulers.io(),
      AndroidSchedulers.mainThread(),
      CompositeDisposable(),
      interactor,
      data,
      buildUpdateIntentUseCase,
      getChangeFiatCurrencyModelUseCase,
      displayChatUseCase,
      getStoredCardsUseCase,
      generateWebLoginUrlUseCase
    )
  }

  @Provides
  fun providesSettingsData(settingsFragment: Fragment): SettingsData {
    settingsFragment.requireArguments()
      .apply {
        return SettingsData(getBoolean(SettingsFragment.TURN_ON_FINGERPRINT, false))
      }
  }
}