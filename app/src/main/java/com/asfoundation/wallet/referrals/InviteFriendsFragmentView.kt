package com.asfoundation.wallet.referrals

import io.reactivex.Observable
import java.math.BigDecimal

interface InviteFriendsFragmentView {
  fun shareLinkClick(): Observable<Any>
  fun appsAndGamesButtonClick(): Observable<Any>
  fun showShare()
  fun navigateToAptoide()
  fun showNotificationCard(pendingAmount: BigDecimal)
  fun changeBottomSheetState()
}