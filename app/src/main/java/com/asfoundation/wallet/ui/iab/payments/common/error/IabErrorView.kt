package com.asfoundation.wallet.ui.iab.payments.common.error

import io.reactivex.Observable

interface IabErrorView {

  fun cancelClickEvent(): Observable<Any>

  fun backClickEvent(): Observable<Any>

  fun setErrorMessage(errorMessage: String)

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>
}