package com.asfoundation.wallet.ui.iab.payments.common.error

import androidx.fragment.app.Fragment
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class IabErrorModule {

  @Provides
  fun providesIabErrorData(fragment: Fragment): IabErrorData {
    fragment.requireArguments()
        .apply {
          val errorMessage = if (containsKey(IabErrorFragment.ERROR_MESSAGE_STRING)) {
            getString(IabErrorFragment.ERROR_MESSAGE_STRING)!!
          } else {
            fragment.getString(getInt(IabErrorFragment.ERROR_MESSAGE_RESOURCE))
          }
          return IabErrorData(errorMessage,
              getString(IabErrorFragment.FEATURE_ENTRY_BACKSTACK_NAME)!!)
        }
  }

  @Provides
  fun providesIabErrorPresenter(fragment: Fragment,
                                data: IabErrorData,
                                navigator: IabErrorNavigator,
                                supportInteractor: SupportInteractor): IabErrorPresenter {
    return IabErrorPresenter(fragment as IabErrorView, data, navigator, supportInteractor,
        CompositeDisposable())
  }
}