package com.asfoundation.wallet.subscriptions.list

import androidx.fragment.app.Fragment
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
class SubscriptionListModule {

  @Provides
  fun providesSubscriptionListPresenter(fragment: Fragment,
                                        data: SubscriptionListData,
                                        interactor: UserSubscriptionsInteractor,
                                        navigator: SubscriptionListNavigator): SubscriptionListPresenter {
    return SubscriptionListPresenter(fragment as SubscriptionListView, data, interactor, navigator,
        CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesSubcriptionListData(fragment: Fragment): SubscriptionListData {
    fragment.requireArguments()
        .apply {
          return SubscriptionListData(getBoolean(SubscriptionListFragment.FRESH_RELOAD_KEY))
        }
  }

  @Provides
  fun providesSubscriptionListNavigator(fragment: Fragment): SubscriptionListNavigator {
    return SubscriptionListNavigator(fragment.requireFragmentManager())
  }
}