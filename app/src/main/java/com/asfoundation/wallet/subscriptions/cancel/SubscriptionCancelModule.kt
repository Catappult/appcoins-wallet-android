package com.asfoundation.wallet.subscriptions.cancel

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class SubscriptionCancelModule {

  @Provides
  fun providesSubscriptionCancelPresenter(
    fragment: Fragment,
    interactor: UserSubscriptionsInteractor,
    data: SubscriptionCancelData,
    navigator: SubscriptionCancelNavigator
  ): SubscriptionCancelPresenter {
    return SubscriptionCancelPresenter(
      fragment as SubscriptionCancelView, interactor, data,
      navigator, CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread()
    )
  }

  @Provides
  fun providesSubscriptionCancelData(fragment: Fragment) =
    fragment.run {
      SubscriptionCancelData(
        subscriptionItem = getSerializableExtra<SubscriptionItem>(SubscriptionCancelFragment.SUBSCRIPTION_ITEM_KEY)!!,
        transitionName = requireArguments().getString(SubscriptionCancelFragment.TRANSITION_NAME_KEY, "")
      )
    }
}