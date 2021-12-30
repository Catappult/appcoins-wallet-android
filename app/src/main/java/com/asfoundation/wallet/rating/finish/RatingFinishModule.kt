package com.asfoundation.wallet.rating.finish

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class RatingFinishModule {

  @Provides
  fun providesRatingFinishPresenter(fragment: Fragment,
                                    navigator: RatingNavigator,
                                    ratingAnalytics: RatingAnalytics): RatingFinishPresenter {
    return RatingFinishPresenter(fragment as RatingFinishView, ratingAnalytics, navigator,
        CompositeDisposable())
  }

//  @Provides
//  fun providesRatingNavigator(fragment: Fragment): RatingNavigator {
//    return RatingNavigator(fragment.activity as RatingActivity, fragment.requireFragmentManager())
//  }
}