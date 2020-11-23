package com.asfoundation.wallet.ui.iab.payments.common.error

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.iab.IabActivity
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class IabErrorModule {

  @Provides
  fun providesIabErrorNavigator(fragment: IabErrorFragment): IabErrorNavigator {
    return IabErrorNavigator(fragment.activity as IabActivity, fragment.requireFragmentManager())
  }

  @Provides
  fun providesIabErrorData(fragment: IabErrorFragment): IabErrorData {
    fragment.arguments!!.apply {
      val errorMessage = if (containsKey(IabErrorFragment.ERROR_MESSAGE_STRING)) {
        getString(IabErrorFragment.ERROR_MESSAGE_STRING)!!
      } else {
        fragment.getString(getInt(IabErrorFragment.ERROR_MESSAGE_RESOURCE))
      }
      return IabErrorData(errorMessage, getString(IabErrorFragment.FEATURE_ENTRY_BACKSTACK_NAME)!!)
    }
  }

  @Provides
  fun providesIabErrorPresenter(fragment: IabErrorFragment,
                                data: IabErrorData,
                                navigator: IabErrorNavigator,
                                walletService: WalletService,
                                supportRepository: SupportRepository,
                                gamification: Gamification): IabErrorPresenter {
    return IabErrorPresenter(fragment as IabErrorView, data, navigator, walletService,
        supportRepository, gamification, CompositeDisposable())
  }
}