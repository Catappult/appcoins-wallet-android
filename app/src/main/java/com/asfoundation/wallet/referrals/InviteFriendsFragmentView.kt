package com.asfoundation.wallet.referrals

import io.reactivex.Observable
import java.math.BigDecimal

interface InviteFriendsFragmentView {
  fun shareLinkClick(): Observable<Any>
  fun showShare()
  fun showNotificationCard(pendingAmount: BigDecimal, symbol: String, icon: Int?)
  fun changeBottomSheetState()
}