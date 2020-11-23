package com.asfoundation.wallet.ui.settings.entry

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.fingerprint.FingerprintPreferenceRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.FingerPrintInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
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
                                data: SettingsData): SettingsPresenter {
    return SettingsPresenter(settingsFragment as SettingsView, navigator, Schedulers.io(),
        AndroidSchedulers.mainThread(), CompositeDisposable(), interactor, data)
  }

  @Provides
  fun providesSettingsData(settingsFragment: SettingsFragment): SettingsData {
    settingsFragment.arguments!!.apply {
      return SettingsData(getBoolean(SettingsFragment.TURN_ON_FINGERPRINT, false))
    }
  }

  @Provides
  fun providesSettingsInteractor(findDefaultWalletInteract: FindDefaultWalletInteract,
                                 supportRepository: SupportRepository,
                                 walletsInteract: WalletsInteract,
                                 autoUpdateInteract: AutoUpdateInteract,
                                 fingerPrintInteractor: FingerPrintInteractor,
                                 walletsEventSender: WalletsEventSender,
                                 preferencesRepositoryType: PreferencesRepositoryType,
                                 fingerprintPreferenceRepository: FingerprintPreferenceRepositoryContract): SettingsInteractor {
    return SettingsInteractor(findDefaultWalletInteract, supportRepository, walletsInteract,
        autoUpdateInteract, fingerPrintInteractor, walletsEventSender, preferencesRepositoryType,
        fingerprintPreferenceRepository)
  }

  @Provides
  fun providesSettingsNavigator(settingsFragment: SettingsFragment): SettingsNavigator {
    return SettingsNavigator(settingsFragment.requireFragmentManager(), settingsFragment.activity!!)
  }
}