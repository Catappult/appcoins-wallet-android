package com.asfoundation.wallet.ui.settings.entry

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.ObserveSendLogsStateUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.ResetSendLogsStateUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.SendLogsUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObservePromoCodeStateUseCase
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
                                observeSendLogsStateUseCase: ObserveSendLogsStateUseCase,
                                resetSendLogsStateUseCase: ResetSendLogsStateUseCase,
                                sendLogsUseCase: SendLogsUseCase,
                                observePromoCodeStateUseCase: ObservePromoCodeStateUseCase): SettingsPresenter {
    return SettingsPresenter(settingsFragment as SettingsView, navigator, Schedulers.io(),
        AndroidSchedulers.mainThread(), CompositeDisposable(), interactor, data,
        getChangeFiatCurrencyModelUseCase,observeSendLogsStateUseCase, resetSendLogsStateUseCase, sendLogsUseCase, observePromoCodeStateUseCase)
  }

  @Provides
  fun providesSettingsData(settingsFragment: Fragment): SettingsData {
    settingsFragment.requireArguments()
        .apply {
          return SettingsData(getBoolean(SettingsFragment.TURN_ON_FINGERPRINT, false))
        }
  }
}