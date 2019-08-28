package com.asfoundation.wallet.referrals

import io.reactivex.Observable

interface InviteFriendsVerificationView {
  fun setDescriptionText(referralValue: String)

  fun beenInvitedClick(): Observable<Any>
  fun verifyButtonClick(): Observable<Any>
  fun navigateToWalletValidation(b: Boolean)
}