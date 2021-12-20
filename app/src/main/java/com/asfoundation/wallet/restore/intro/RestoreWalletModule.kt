package com.asfoundation.wallet.restore.intro

import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.navigator.ActivityNavigatorContract
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.restore.RestoreWalletActivity
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class RestoreWalletModule {

  @Provides
  fun providesRestoreWalletNavigator(fragment: RestoreWalletFragment,
                                     activityNavigator: ActivityNavigatorContract): RestoreWalletNavigator {
    return RestoreWalletNavigator(fragment.requireFragmentManager(), activityNavigator)
  }

  @Provides
  fun providesRestoreWalletPresenter(fragment: RestoreWalletFragment,
                                     navigator: RestoreWalletNavigator,
                                     updateWalletInfoUseCase: UpdateWalletInfoUseCase,
                                     interactor: RestoreWalletInteractor, logger: Logger,
                                     eventSender: WalletsEventSender,
                                     setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
                                     rxSchedulers: RxSchedulers): RestoreWalletPresenter {
    return RestoreWalletPresenter(fragment as RestoreWalletView, CompositeDisposable(), navigator,
        interactor, updateWalletInfoUseCase, eventSender, logger, setOnboardingCompletedUseCase,
        rxSchedulers)
  }

  @Provides
  fun providesNavigator(fragment: RestoreWalletFragment): ActivityNavigatorContract {
    return fragment.activity as RestoreWalletActivity
  }
}