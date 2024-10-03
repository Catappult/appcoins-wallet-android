package com.asfoundation.wallet.subscriptions.success

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
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
  fun providesSubscriptionSuccessPresenter(
    fragment: Fragment,
    data: SubscriptionSuccessData,
    navigator: SubscriptionSuccessNavigator
  ): SubscriptionSuccessPresenter {
    return SubscriptionSuccessPresenter(
      view = fragment as SubscriptionSuccessView,
      data = data,
      navigator = navigator,
      disposables = CompositeDisposable(),
      viewScheduler = AndroidSchedulers.mainThread()
    )
  }

  @Provides
  fun providesSubscriptionSuccessData(fragment: Fragment) =
    SubscriptionSuccessData(
      successType = fragment.getSerializableExtra<SubscriptionSuccessFragment.SubscriptionSuccess>(
        SubscriptionSuccessFragment.SUCCESS_TYPE_KEY
      )!!
    )
}