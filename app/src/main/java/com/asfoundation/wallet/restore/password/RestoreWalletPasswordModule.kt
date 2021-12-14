package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.restore.intro.RestoreWalletInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class RestoreWalletPasswordModule {

  @Provides
  fun providesRestoreWalletPasswordPresenter(fragment: RestoreWalletPasswordFragment,
                                             data: RestoreWalletPasswordData,
                                             observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                                             interactor: RestoreWalletPasswordInteractor,
                                             eventSender: WalletsEventSender,
                                             currencyFormatUtils: CurrencyFormatUtils,
                                             setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
                                             rxSchedulers: RxSchedulers): RestoreWalletPasswordPresenter {
    return RestoreWalletPasswordPresenter(fragment as RestoreWalletPasswordView, data,
        observeWalletInfoUseCase, interactor,
        eventSender, currencyFormatUtils, setOnboardingCompletedUseCase,
        CompositeDisposable(), rxSchedulers)
  }

  @Provides
  fun providesRestoreWalletPasswordData(
      fragment: RestoreWalletPasswordFragment): RestoreWalletPasswordData {
    fragment.requireArguments()
        .apply {
          return RestoreWalletPasswordData(
              getString(RestoreWalletPasswordFragment.KEYSTORE_KEY, ""))
        }
  }

  @Provides
  fun provideRestoreWalletPasswordInteractor(gson: Gson,
                                             restoreWalletInteractor: RestoreWalletInteractor): RestoreWalletPasswordInteractor {
    return RestoreWalletPasswordInteractor(gson, restoreWalletInteractor)
  }
}