package com.asfoundation.wallet.ui.overlay

import com.asfoundation.wallet.repository.ImpressionPreferencesRepositoryType

class OverlayInteractor(
    private val impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType) {

  fun setHasSeenPromotionTooltip() =
      impressionPreferencesRepositoryType.setHasSeenPromotionTooltip()

  fun setHasSeenVoucherTooltip() = impressionPreferencesRepositoryType.setHasSeenVoucherTooltip()
}
