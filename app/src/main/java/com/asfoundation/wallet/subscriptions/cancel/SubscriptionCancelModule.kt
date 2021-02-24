package com.asfoundation.wallet.subscriptions.cancel

import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class SubscriptionCancelModule {

  @Provides
  fun providesSubscriptionCancelPresenter(fragment: SubscriptionCancelFragment,
                                          interactor: UserSubscriptionsInteractor,
                                          data: SubscriptionCancelData,
                                          navigator: SubscriptionCancelNavigator): SubscriptionCancelPresenter {
    return SubscriptionCancelPresenter(fragment as SubscriptionCancelView, interactor, data,
        navigator, CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubscriptionCancelData(fragment: SubscriptionCancelFragment): SubscriptionCancelData {
    fragment.arguments!!.apply {
      return SubscriptionCancelData(
          getSerializable(SubscriptionCancelFragment.SUBSCRIPTION_ITEM_KEY) as SubscriptionItem,
          getString(SubscriptionCancelFragment.TRANSITION_NAME_KEY, ""))
    }
  }

  @Provides
  fun providesSubscriptionCancelNavigator(
      fragment: SubscriptionCancelFragment): SubscriptionCancelNavigator {
    return SubscriptionCancelNavigator(fragment.requireFragmentManager(), fragment.activity!!)
  }
}