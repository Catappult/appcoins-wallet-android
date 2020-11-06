package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
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
}

@Module
abstract class RestoreWalletPasswordViewModule {
  @Binds
  abstract fun bindRestoreWalletPasswordView(
      fragment: RestoreWalletPasswordFragment): RestoreWalletPasswordView
}