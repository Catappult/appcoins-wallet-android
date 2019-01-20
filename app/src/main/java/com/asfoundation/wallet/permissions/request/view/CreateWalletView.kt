package com.asfoundation.wallet.permissions.request.view

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

interface CreateWalletView {
  fun getOnCreateWalletClick(): Observable<Any>
  fun closeSuccess()
  fun getCancelClick(): Observable<Any>
  fun closeCancel()
  fun showLoading()
  fun getFinishAnimationFinishEvent(): BehaviorRelay<Any>
  fun showFinishAnimation()
}
