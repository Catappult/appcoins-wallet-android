package com.asfoundation.wallet.ui.overlay

import repository.CommonsPreferencesDataSource
import javax.inject.Inject

class OverlayInteractor @Inject constructor(private val commonsPreferencesDataSource: CommonsPreferencesDataSource) {

  fun setHasSeenPromotionTooltip() = commonsPreferencesDataSource.setHasSeenPromotionTooltip()
}
