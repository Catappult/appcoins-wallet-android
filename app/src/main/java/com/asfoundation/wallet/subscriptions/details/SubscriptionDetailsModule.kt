package com.asfoundation.wallet.subscriptions.details

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

@InstallIn(FragmentComponent::class)
@Module
class SubscriptionDetailsModule {

  @Provides
  fun providesSubscriptionDetailsPresenter(
    fragment: Fragment,
    navigator: SubscriptionDetailsNavigator,
    userSubscriptionsInteractor: UserSubscriptionsInteractor,
    data: SubscriptionDetailsData
  ): SubscriptionDetailsPresenter {
    return SubscriptionDetailsPresenter(
      fragment as SubscriptionDetailsView, navigator, data,
      userSubscriptionsInteractor, CompositeDisposable(), AndroidSchedulers.mainThread()
    )
  }

  @Provides
  fun providesSubscriptionDetailsData(fragment: Fragment): SubscriptionDetailsData =
    fragment.run {
      SubscriptionDetailsData(
        subscriptionItem = getSerializableExtra<SubscriptionItem>(SubscriptionDetailsFragment.SUBSCRIPTION_ITEM_KEY)!!,
        transitionName = requireArguments().getString(SubscriptionDetailsFragment.TRANSITION_NAME_KEY, "")
      )
    }
}