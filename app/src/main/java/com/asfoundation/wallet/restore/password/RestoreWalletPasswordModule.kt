package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.restore.intro.RestoreWalletInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class RestoreWalletPasswordModule {

  @Provides
  fun providesRestoreWalletPasswordPresenter(fragment: RestoreWalletPasswordFragment,
                                             data: RestoreWalletPasswordData,
                                             observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                                             interactor: RestoreWalletPasswordInteractor,
                                             eventSender: WalletsEventSender,
                                             currencyFormatUtils: CurrencyFormatUtils,
                                             preferencesRepositoryType: PreferencesRepositoryType): RestoreWalletPasswordPresenter {
    return RestoreWalletPasswordPresenter(fragment as RestoreWalletPasswordView, data, observeWalletInfoUseCase, interactor,
        eventSender, currencyFormatUtils, preferencesRepositoryType,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io(),
        Schedulers.computation())
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