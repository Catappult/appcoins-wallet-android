package com.asfoundation.wallet.subscriptions.cancelsuccess

import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@Module
class SubscriptionCancelSuccessModule {

  @Provides
  fun providesSubscriptionCancelSuccessPresenter(
      fragment: SubscriptionCancelSuccessFragment,
      navigator: SubscriptionCancelSuccessNavigator): SubscriptionCancelSuccessPresenter {
    return SubscriptionCancelSuccessPresenter(fragment as SubscriptionCancelSuccessView, navigator,
        CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubscriptionCancelSuccessNavigator(
      fragment: SubscriptionCancelSuccessFragment): SubscriptionCancelSuccessNavigator {
    return SubscriptionCancelSuccessNavigator(fragment.requireFragmentManager(),
        fragment.activity!!)
  }
}