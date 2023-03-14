package com.asfoundation.wallet.ui.overlay

import repository.PreferencesRepositoryType
import javax.inject.Inject

class OverlayInteractor @Inject constructor(private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun setHasSeenPromotionTooltip() = preferencesRepositoryType.setHasSeenPromotionTooltip()
}
