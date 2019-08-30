package com.asfoundation.wallet.referrals

import io.reactivex.Observable
import java.math.BigDecimal

interface InviteFriendsFragmentView {
  fun setTextValues(individualValue: BigDecimal, pendingValue: BigDecimal, currency: String)
  fun shareLinkClick(): Observable<Any>
  fun appsAndGamesButtonClick(): Observable<Any>
  fun showShare(link: String)
  fun navigateToAptoide()
  fun showNotificationCard(pendingAmount: BigDecimal)
}