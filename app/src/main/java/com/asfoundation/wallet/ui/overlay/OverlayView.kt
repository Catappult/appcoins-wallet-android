package com.asfoundation.wallet.ui.overlay

import io.reactivex.Observable

interface OverlayView {

  fun discoverClick(): Observable<Any>

  fun dismissClick(): Observable<Any>

  fun dismissView()

  fun overlayClick(): Observable<Any>
}
