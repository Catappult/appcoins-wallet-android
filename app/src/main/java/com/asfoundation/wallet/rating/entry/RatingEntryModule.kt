package com.asfoundation.wallet.rating.entry

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
class RatingEntryModule {

  @Provides
  fun providesRatingEntryPresenter(fragment: RatingEntryFragment,
                                   navigator: RatingNavigator,
                                   interactor: RatingInteractor,
                                   ratingAnalytics: RatingAnalytics): RatingEntryPresenter {
    return RatingEntryPresenter(fragment, navigator, interactor, ratingAnalytics,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesRatingNavigator(fragment: RatingEntryFragment): RatingNavigator {
    return RatingNavigator(fragment.activity as RatingActivity, fragment.requireFragmentManager())
  }
}
