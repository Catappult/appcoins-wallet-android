package com.asfoundation.wallet.referrals

import io.reactivex.Observable

interface ReferralsView {
  fun setupLayout()
  fun bottomSheetHeaderClick(): Observable<Any>
  fun changeBottomSheetState()
}
