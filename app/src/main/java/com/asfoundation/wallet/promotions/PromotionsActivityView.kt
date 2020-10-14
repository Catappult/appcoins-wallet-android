package com.asfoundation.wallet.promotions

import io.reactivex.Observable

interface PromotionsActivityView {

  fun navigateToGamification(bonus: Double)

  fun navigateToInviteFriends()

  fun handleShare(link: String)

  fun openDetailsLink(url: String)

  fun backPressed(): Observable<Any>

  fun enableBack()

  fun disableBack()
}
