package com.asfoundation.wallet.rating.finish

import com.asfoundation.wallet.rating.RatingAnalytics
import com.asfoundation.wallet.rating.RatingNavigator
import io.reactivex.disposables.CompositeDisposable

class RatingFinishPresenter(private val view: RatingFinishView,
                            private val ratingAnalytics: RatingAnalytics,
                            private val navigator: RatingNavigator,
                            private val disposables: CompositeDisposable) {

  fun present() {
    ratingAnalytics.sendFinishEvent()
    handleAnimationEnd()
  }

  private fun handleAnimationEnd() {
    disposables.add(
        view.animationEndEvent()
            .doOnNext { navigator.closeActivity() }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()

}