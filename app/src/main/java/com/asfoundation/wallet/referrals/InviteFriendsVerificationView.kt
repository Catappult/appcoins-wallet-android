package com.asfoundation.wallet.referrals

import io.reactivex.Observable

interface InviteFriendsVerificationView {
  fun beenInvitedClick(): Observable<Any>
  fun verifyButtonClick(): Observable<Any>
  fun navigateToWalletValidation(beenInvited: Boolean)
}