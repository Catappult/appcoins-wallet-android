package com.asfoundation.wallet.rating.entry

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.rating.RatingActivity
import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingInteractor
import com.asfoundation.wallet.rating.RatingNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class RatingEntryModule {

  @Provides
  fun providesRatingEntryPresenter(fragment: Fragment,
                                   navigator: RatingNavigator,
                                   interactor: RatingInteractor,
                                   ratingAnalytics: RatingAnalytics): RatingEntryPresenter {
    return RatingEntryPresenter(fragment as RatingEntryView, navigator, interactor, ratingAnalytics,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesRatingNavigator(fragment: Fragment): RatingNavigator {
    return RatingNavigator(fragment.activity as RatingActivity, fragment.requireFragmentManager())
  }
}
