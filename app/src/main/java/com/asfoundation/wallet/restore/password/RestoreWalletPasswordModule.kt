package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.restore.intro.RestoreWalletInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class RestoreWalletPasswordModule {

  @Provides
  fun providesRestoreWalletPasswordPresenter(view: RestoreWalletPasswordView,
                                             data: RestoreWalletPasswordData,
                                             interactor: RestoreWalletPasswordInteractor,
                                             eventSender: WalletsEventSender): RestoreWalletPasswordPresenter {
    return RestoreWalletPasswordPresenter(view, data, interactor, eventSender,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io(),
        Schedulers.computation())
  }

  @Provides
  fun providesRestoreWalletPasswordData(
      fragment: RestoreWalletPasswordFragment): RestoreWalletPasswordData {
    fragment.arguments!!.apply {
      return RestoreWalletPasswordData(getString(RestoreWalletPasswordFragment.KEYSTORE_KEY, ""))
    }
  }

  @Provides
  fun provideRestoreWalletPasswordInteractor(gson: Gson, balanceInteractor: BalanceInteractor,
                                             restoreWalletInteractor: RestoreWalletInteractor): RestoreWalletPasswordInteractor {
    return RestoreWalletPasswordInteractor(gson, balanceInteractor, restoreWalletInteractor)
  }
}

@Module
abstract class RestoreWalletPasswordViewModule {
  @Binds
  abstract fun bindRestoreWalletPasswordView(
      fragment: RestoreWalletPasswordFragment): RestoreWalletPasswordView
}