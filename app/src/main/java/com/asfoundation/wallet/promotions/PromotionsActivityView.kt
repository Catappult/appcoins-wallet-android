package com.asfoundation.wallet.promotions

interface PromotionsActivityView {

  fun navigateToLegacyGamification(bonus: Double)

  fun navigateToGamification(bonus: Double)

  fun navigateToInviteFriends()

  fun handleShare(link: String)
}
