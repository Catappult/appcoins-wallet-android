package com.asfoundation.wallet.referrals

import io.reactivex.Observable

interface InviteFriendsActivityView {

  fun navigateToVerificationFragment()
  fun navigateToInviteFriends()
  fun getInfoButtonClick(): Observable<Any>
  fun infoButtonInitialized(): Observable<Boolean>
  fun showNoNetworkScreen()
  fun showInfoButton()
  fun navigateToWalletValidation(beenInvited: Boolean)
  fun showShare()
  fun navigateToTopApps()
}