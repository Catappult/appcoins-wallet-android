package com.asfoundation.wallet.promotions

import io.reactivex.Observable

interface PromotionsActivityView {

  fun navigateToGamification(bonus: Double)

  fun navigateToInviteFriends()

  fun handleShare(link: String)

  fun opendetailsLink(url: String)

  fun backPressed(): Observable<Any>

  fun enableBack()

  fun disableBack()
}
