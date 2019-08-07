package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface GamificationView {
  fun closeHowItWorksView()
  fun showHowItWorksButton()
  fun onHowItWorksClosed()
  fun getInfoButtonClick(): Observable<Any>?
}
