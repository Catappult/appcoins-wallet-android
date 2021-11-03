package com.asfoundation.wallet.subscriptions.success

import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@Module
class SubscriptionSuccessModule {

  @Provides
  fun providesSubscriptionSuccessPresenter(
      fragment: SubscriptionSuccessFragment,
      data: SubscriptionSuccessData,
      navigator: SubscriptionSuccessNavigator): SubscriptionSuccessPresenter {
    return SubscriptionSuccessPresenter(fragment as SubscriptionSuccessView, data, navigator,
        CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubscriptionSuccessData(
      fragment: SubscriptionSuccessFragment): SubscriptionSuccessData {
    fragment.requireArguments()
        .apply {
          return SubscriptionSuccessData(getSerializable(
              SubscriptionSuccessFragment.SUCCESS_TYPE_KEY)!! as SubscriptionSuccessFragment.SubscriptionSuccess)
        }
  }

  @Provides
  fun providesSubscriptionSuccessNavigator(
      fragment: SubscriptionSuccessFragment): SubscriptionSuccessNavigator {
    return SubscriptionSuccessNavigator(fragment.requireFragmentManager())
  }
}