package com.asfoundation.wallet.ui.iab.payments.common.error

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class IabErrorModule {

  @Provides
  fun providesIabErrorNavigator(fragment: IabErrorFragment): IabErrorNavigator {
    return IabErrorNavigator(fragment.requireActivity(), fragment.requireFragmentManager())
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
  fun providesIabErrorPresenter(view: IabErrorView,
                                data: IabErrorData,
                                navigator: IabErrorNavigator): IabErrorPresenter {
    return IabErrorPresenter(view, data, navigator, CompositeDisposable())
  }
}

@Module
abstract class IabErrorViewModule {
  @Binds
  abstract fun bindIabErrorView(
      fragment: IabErrorFragment): IabErrorView
}