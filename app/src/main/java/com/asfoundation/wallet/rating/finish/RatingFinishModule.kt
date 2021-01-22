package com.asfoundation.wallet.rating.finish

import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingNavigator
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class RatingFinishModule {

  @Provides
  fun providesRatingFinishPresenter(fragment: RatingFinishFragment,
                                    navigator: RatingNavigator,
                                    ratingAnalytics: RatingAnalytics): RatingFinishPresenter {
    return RatingFinishPresenter(fragment, ratingAnalytics, navigator, CompositeDisposable())
  }

  @Provides
  fun providesRatingNavigator(fragment: RatingFinishFragment): RatingNavigator {
    return RatingNavigator(fragment.activity as RatingActivity, fragment.requireFragmentManager())
  }
}