package com.asfoundation.wallet.promotions

interface PromotionsActivityView {

  fun navigateToLegacyGamification()

  fun navigateToGamification()

  fun navigateToInviteFriends()

  fun handleShare(link: String)
}
