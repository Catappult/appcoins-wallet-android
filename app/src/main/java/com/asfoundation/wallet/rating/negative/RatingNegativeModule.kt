package com.asfoundation.wallet.rating.negative

import androidx.fragment.app.Fragment
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
class RatingNegativeModule {

  @Provides
  fun providesNegativePresenter(fragment: Fragment,
                                interactor: RatingInteractor, navigator: RatingNavigator,
                                analytics: RatingAnalytics): RatingNegativePresenter {
    return RatingNegativePresenter(fragment as RatingNegativeView, interactor, navigator, analytics,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }
}
