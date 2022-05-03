package com.asfoundation.wallet.ui.settings.entry

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetUpdatedPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObservePromoCodeUseCase
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
  fun providesSettingsPresenter(settingsFragment: Fragment,
                                navigator: SettingsNavigator,
                                interactor: SettingsInteractor,
                                data: SettingsData,
                                getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
                                getUpdatedPromoCodeUseCase: GetUpdatedPromoCodeUseCase,
                                observePromoCodeUseCase: ObservePromoCodeUseCase): SettingsPresenter {
    return SettingsPresenter(settingsFragment as SettingsView, navigator, Schedulers.io(),
        AndroidSchedulers.mainThread(), CompositeDisposable(), interactor, data,
        getChangeFiatCurrencyModelUseCase, getUpdatedPromoCodeUseCase, observePromoCodeUseCase)
  }

  @Provides
  fun providesSettingsData(settingsFragment: Fragment): SettingsData {
    settingsFragment.requireArguments()
        .apply {
          return SettingsData(getBoolean(SettingsFragment.TURN_ON_FINGERPRINT, false))
        }
  }
}