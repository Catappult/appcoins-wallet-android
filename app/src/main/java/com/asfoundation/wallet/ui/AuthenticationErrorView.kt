package com.asfoundation.wallet.ui

import io.reactivex.Observable

interface AuthenticationErrorView {

  fun outsideOfBottomSheetClick(): Observable<Any>

  fun showBottomSheet()

  fun retryAuthentication()
}
