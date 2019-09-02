package com.asfoundation.wallet.referrals

import io.reactivex.Observable

interface InviteFriendsActivityView {

  fun navigateToVerificationFragment()
  fun navigateToInviteFriends()
  fun getInfoButtonClick(): Observable<Any>
  fun infoButtonInitialized(): Observable<Boolean>
  fun showInfoButton()
  fun navigateToWalletValidation(beenInvited: Boolean)
  fun showShare(link: String)
  fun navigateToTopApps()
  fun showNetworkErrorView()
  fun showRetryAnimation()
  fun retryClick(): Observable<Any>
}