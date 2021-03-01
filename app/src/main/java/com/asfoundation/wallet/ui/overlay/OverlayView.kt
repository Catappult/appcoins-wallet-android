package com.asfoundation.wallet.ui.overlay

import io.reactivex.Observable

interface OverlayView {

  fun initializeView(bottomNavigationItem: Int, type: OverlayType)

  fun discoverClick(): Observable<Any>

  fun dismissClick(): Observable<Any>

  fun navigateToPromotions()

  fun dismissView()

  fun overlayClick(): Observable<Any>
}
