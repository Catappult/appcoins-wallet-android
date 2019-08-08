package com.asfoundation.wallet.ui.gamification

import io.reactivex.Observable

interface GamificationView {
  fun getInfoButtonClick(): Observable<Any>?
}
