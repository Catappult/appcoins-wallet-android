package com.asfoundation.wallet.promotions

interface PromotionsActivityView {

  fun navigateToGamification(bonus: Double)

  fun navigateToInviteFriends()

  fun handleShare(link: String)

  fun openDetailsUrl(url: String)
}
