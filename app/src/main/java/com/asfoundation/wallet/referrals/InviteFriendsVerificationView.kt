package com.asfoundation.wallet.referrals

import io.reactivex.Observable
import java.math.BigDecimal

interface InviteFriendsVerificationView {
  fun setDescriptionText(referralValue: BigDecimal, currency: String)
  fun beenInvitedClick(): Observable<Any>
  fun verifyButtonClick(): Observable<Any>
  fun navigateToWalletValidation(beenInvited: Boolean)
}