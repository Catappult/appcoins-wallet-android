package com.asfoundation.wallet.rating.positive

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
class RatingPositiveModule {

  @Provides
  fun providesRatingPositivePresenter(fragment: Fragment,
                                      navigator: RatingNavigator,
                                      interactor: RatingInteractor,
                                      analytics: RatingAnalytics): RatingPositivePresenter {
    return RatingPositivePresenter(fragment as RatingPositiveView, navigator, interactor, analytics,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }
}
