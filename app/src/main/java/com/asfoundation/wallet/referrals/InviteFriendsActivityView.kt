package com.asfoundation.wallet.referrals

import io.reactivex.Observable
import java.math.BigDecimal

interface InviteFriendsActivityView {

  fun navigateToVerificationFragment(amount: BigDecimal, currency: String)

  fun navigateToInviteFriends(amount: BigDecimal, pendingAmount: BigDecimal, currency: String,
                              link: String?, completed: Int, receivedAmount: BigDecimal,
                              maxAmount: BigDecimal, available: Int, isRedeemed: Boolean)

  fun getInfoButtonClick(): Observable<Any>
  fun infoButtonInitialized(): Observable<Boolean>
  fun showInfoButton()
  fun navigateToWalletValidation(beenInvited: Boolean)
  fun showShare(link: String)
  fun showNetworkErrorView()
  fun showRetryAnimation()
  fun retryClick(): Observable<Any>
}