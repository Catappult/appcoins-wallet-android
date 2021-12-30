package com.asfoundation.wallet.subscriptions.success

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class SubscriptionSuccessModule {

  @Provides
  fun providesSubscriptionSuccessPresenter(fragment: Fragment,
                                           data: SubscriptionSuccessData,
                                           navigator: SubscriptionSuccessNavigator): SubscriptionSuccessPresenter {
    return SubscriptionSuccessPresenter(fragment as SubscriptionSuccessView, data, navigator,
        CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubscriptionSuccessData(fragment: Fragment): SubscriptionSuccessData {
    fragment.requireArguments()
        .apply {
          return SubscriptionSuccessData(getSerializable(
              SubscriptionSuccessFragment.SUCCESS_TYPE_KEY)!! as SubscriptionSuccessFragment.SubscriptionSuccess)
        }
  }

  @Provides
  fun providesSubscriptionSuccessNavigator(fragment: Fragment): SubscriptionSuccessNavigator {
    return SubscriptionSuccessNavigator(fragment.requireFragmentManager())
  }
}