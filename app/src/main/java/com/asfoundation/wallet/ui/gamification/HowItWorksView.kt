package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface HowItWorksView {
  fun getOkClick(): Observable<Any>
  fun close()
  fun showLevels(levels: List<ViewLevel>)
}
