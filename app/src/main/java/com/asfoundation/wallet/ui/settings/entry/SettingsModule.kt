package com.asfoundation.wallet.ui.settings.entry

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.FingerprintInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class SettingsModule {

  @Provides
  fun providesSettingsPresenter(settingsFragment: SettingsFragment,
                                navigator: SettingsNavigator,
                                interactor: SettingsInteractor,
                                data: SettingsData,
                                getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase): SettingsPresenter {
    return SettingsPresenter(settingsFragment as SettingsView, navigator, Schedulers.io(),
        AndroidSchedulers.mainThread(), CompositeDisposable(), interactor, data,
        getSelectedCurrencyUseCase)
  }

  @Provides
  fun providesSettingsData(settingsFragment: SettingsFragment): SettingsData {
    settingsFragment.requireArguments()
        .apply {
          return SettingsData(getBoolean(SettingsFragment.TURN_ON_FINGERPRINT, false))
        }
  }

  @Provides
  fun providesSettingsInteractor(findDefaultWalletInteract: FindDefaultWalletInteract,
                                 supportInteractor: SupportInteractor,
                                 walletsInteract: WalletsInteract,
                                 autoUpdateInteract: AutoUpdateInteract,
                                 fingerprintInteractor: FingerprintInteractor,
                                 walletsEventSender: WalletsEventSender,
                                 preferencesRepositoryType: PreferencesRepositoryType,
                                 fingerprintPreferencesRepository: FingerprintPreferencesRepositoryContract): SettingsInteractor {
    return SettingsInteractor(findDefaultWalletInteract, supportInteractor, walletsInteract,
        autoUpdateInteract, fingerprintInteractor, walletsEventSender, preferencesRepositoryType,
        fingerprintPreferencesRepository)
  }

  @Provides
  fun providesSettingsNavigator(settingsFragment: SettingsFragment): SettingsNavigator {
    return SettingsNavigator(settingsFragment.requireFragmentManager(),
        settingsFragment.requireActivity())
  }
}