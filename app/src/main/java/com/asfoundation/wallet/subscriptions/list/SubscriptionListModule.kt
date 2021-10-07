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
                                        data: SubscriptionListData,
                                        interactor: UserSubscriptionsInteractor,
                                        navigator: SubscriptionListNavigator): SubscriptionListPresenter {
    return SubscriptionListPresenter(fragment as SubscriptionListView, data, interactor, navigator,
        CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubcriptionListData(fragment: SubscriptionListFragment): SubscriptionListData {
    fragment.arguments!!.apply {
      return SubscriptionListData(getBoolean(SubscriptionListFragment.FRESH_RELOAD_KEY))
    }
  }

  @Provides
  fun providesSubscriptionListNavigator(
      fragment: SubscriptionListFragment): SubscriptionListNavigator {
    return SubscriptionListNavigator(fragment.requireFragmentManager())
  }
}