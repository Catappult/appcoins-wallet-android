package com.asfoundation.wallet.subscriptions.list

import com.asfoundation.wallet.subscriptions.UserSubscriptionsInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class SubscriptionListModule {

  @Provides
  fun providesSubscriptionListPresenter(fragment: SubscriptionListFragment,
                                        interactor: UserSubscriptionsInteractor,
                                        navigator: SubscriptionListNavigator): SubscriptionListPresenter {
    return SubscriptionListPresenter(fragment as SubscriptionListView, interactor, navigator,
        CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubscriptionListNavigator(
      fragment: SubscriptionListFragment): SubscriptionListNavigator {
    return SubscriptionListNavigator(fragment.requireFragmentManager())
  }
}