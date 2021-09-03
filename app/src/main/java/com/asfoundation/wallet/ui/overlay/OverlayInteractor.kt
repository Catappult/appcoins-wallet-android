package com.asfoundation.wallet.ui.overlay

import com.asfoundation.wallet.repository.PreferencesRepositoryType

class OverlayInteractor(private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun setHasSeenPromotionTooltip() = preferencesRepositoryType.setHasSeenPromotionTooltip()
}
