package com.asfoundation.wallet.ui

import io.reactivex.Observable

interface AuthenticationErrorBottomSheetView {

  fun getButtonClick(): Observable<Any>

  fun retryAuthentication()

  fun setMessage()

  fun setupUi()
}
