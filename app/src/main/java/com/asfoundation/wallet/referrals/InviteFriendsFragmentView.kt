package com.asfoundation.wallet.referrals

import io.reactivex.Observable

interface InviteFriendsFragmentView {
  fun setTextValues(individualValue: String, pendingValue: String)
  fun shareLinkClick(): Observable<Any>
  fun appsAndGamesButtonClick(): Observable<Any>
  fun showShare()
  fun navigateToAptoide()
}