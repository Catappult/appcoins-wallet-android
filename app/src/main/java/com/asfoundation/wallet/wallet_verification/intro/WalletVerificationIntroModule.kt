package com.asfoundation.wallet.wallet_verification.intro

import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.ActivityNavigatorContract
import com.asfoundation.wallet.wallet_verification.WalletVerificationActivity
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class WalletVerificationIntroModule {

  @Provides
  fun providesWalletVerificationIntroNavigator(fragment: WalletVerificationIntroFragment,
                                               activityNavigator: ActivityNavigatorContract): WalletVerificationIntroNavigator {
    return WalletVerificationIntroNavigator(fragment.requireFragmentManager(), activityNavigator)
  }

  @Provides
  fun providesWalletVerificationIntroPresenter(fragment: WalletVerificationIntroFragment,
                                               navigator: WalletVerificationIntroNavigator,
                                               logger: Logger): WalletVerificationIntroPresenter {
    return WalletVerificationIntroPresenter(fragment as WalletVerificationIntroView,
        CompositeDisposable(), navigator,
        logger, AndroidSchedulers.mainThread(), Schedulers.computation()
    )
  }

  @Provides
  fun providesNavigator(fragment: WalletVerificationIntroFragment): ActivityNavigatorContract {
    return fragment.activity as WalletVerificationActivity
  }
}