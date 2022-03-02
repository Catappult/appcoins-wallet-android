package com.asfoundation.wallet.restore.intro

import androidx.fragment.app.Fragment
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.navigator.ActivityNavigatorContract
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.restore.RestoreWalletActivity
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class RestoreWalletModule {

  @Provides
  fun providesRestoreWalletPresenter(fragment: Fragment,
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
  fun providesNavigator(fragment: Fragment): ActivityNavigatorContract {
    return fragment.activity as RestoreWalletActivity
  }
}