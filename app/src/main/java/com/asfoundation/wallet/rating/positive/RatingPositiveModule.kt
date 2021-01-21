package com.asfoundation.wallet.rating.positive

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
class RatingPositiveModule {

  @Provides
  fun providesRatingPositivePresenter(fragment: RatingPositiveFragment,
                                      navigator: RatingNavigator,
                                      interactor: RatingInteractor,
                                      analytics: RatingAnalytics): RatingPositivePresenter {
    return RatingPositivePresenter(fragment, navigator, interactor, analytics,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesRatingNavigator(fragment: RatingPositiveFragment): RatingNavigator {
    return RatingNavigator(fragment.activity as RatingActivity, fragment.requireFragmentManager())
  }
}
