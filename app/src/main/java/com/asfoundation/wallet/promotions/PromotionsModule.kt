package com.asfoundation.wallet.promotions

import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class PromotionsModule {

  @Provides
  fun providesPromotionsPresenter(fragment: PromotionsFragment,
                                  navigator: PromotionsNavigator,
                                  interactor: PromotionsInteractor): PromotionsPresenter {
    return PromotionsPresenter(fragment, navigator, interactor, CompositeDisposable(),
        Schedulers.io(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesPromotionsNavigator(fragment: PromotionsFragment): PromotionsNavigator {
    return PromotionsNavigator(fragment)
  }
}