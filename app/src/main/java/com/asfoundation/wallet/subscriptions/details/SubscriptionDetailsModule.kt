package com.asfoundation.wallet.subscriptions.details

import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@Module
class SubscriptionDetailsModule {

  @Provides
  fun providesSubscriptionDetailsPresenter(fragment: SubscriptionDetailsFragment,
                                           navigator: SubscriptionDetailsNavigator,
                                           userSubscriptionsInteractor: UserSubscriptionsInteractor,
                                           data: SubscriptionDetailsData): SubscriptionDetailsPresenter {
    return SubscriptionDetailsPresenter(fragment as SubscriptionDetailsView, navigator, data,
        userSubscriptionsInteractor, CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubscriptionDetailsData(
      fragment: SubscriptionDetailsFragment): SubscriptionDetailsData {
    fragment.arguments!!.apply {
      return SubscriptionDetailsData(
          getSerializable(SubscriptionDetailsFragment.SUBSCRIPTION_ITEM_KEY) as SubscriptionItem,
          getString(SubscriptionDetailsFragment.TRANSITION_NAME_KEY, ""))
    }
  }

  @Provides
  fun providesSubscriptionDetailsNavigator(
      fragment: SubscriptionDetailsFragment): SubscriptionDetailsNavigator {
    return SubscriptionDetailsNavigator(fragment.requireFragmentManager())
  }
}