package com.asfoundation.wallet.rating.negative

import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingNavigator
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class RatingNegativeModule {

  @Provides
  fun providesNegativePresenter(fragment: RatingNegativeFragment,
                                interactor: RatingInteractor, navigator: RatingNavigator,
                                analytics: RatingAnalytics): RatingNegativePresenter {
    return RatingNegativePresenter(fragment, interactor, navigator, analytics,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesRatingNavigator(fragment: RatingNegativeFragment): RatingNavigator {
    return RatingNavigator(fragment.activity as RatingActivity, fragment.requireFragmentManager())
  }
}
