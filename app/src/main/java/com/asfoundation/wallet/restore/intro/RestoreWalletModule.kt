package com.asfoundation.wallet.restore.intro

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.ActivityNavigatorContract
import com.asfoundation.wallet.restore.RestoreWalletActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class RestoreWalletModule {

  @Provides
  fun providesRestoreWalletNavigator(fragment: RestoreWalletFragment,
                                     activityNavigator: ActivityNavigatorContract): RestoreWalletNavigator {
    return RestoreWalletNavigator(fragment.requireFragmentManager(), activityNavigator)
  }

  @Provides
  fun providesRestoreWalletPresenter(view: RestoreWalletView, navigator: RestoreWalletNavigator,
                                     interactor: RestoreWalletInteractor, logger: Logger,
                                     eventSender: WalletsEventSender): RestoreWalletPresenter {
    return RestoreWalletPresenter(view, CompositeDisposable(), navigator, interactor, eventSender,
        logger, AndroidSchedulers.mainThread(), Schedulers.computation()
    )
  }

  @Provides
  fun providesNavigator(fragment: RestoreWalletFragment): ActivityNavigatorContract {
    return fragment.activity as RestoreWalletActivity
  }
}

@Module
abstract class RestoreWalletViewModule {
  @Binds
  abstract fun bindRestoreWalletView(fragment: RestoreWalletFragment): RestoreWalletView
}