package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import android.graphics.drawable.Drawable
import io.reactivex.Observable
import java.math.BigDecimal

interface CarrierFeeView {

  fun initializeView(currency: String, fiatAmount: BigDecimal, appcAmount: BigDecimal,
                     skuDescription: String, bonusAmount: BigDecimal?, carrierName: String,
                     carrierImage: String, carrierFeeFiat: BigDecimal)

  fun setAppDetails(appName: String, icon: Drawable)

  fun cancelButtonEvent(): Observable<Any>

  fun systemBackEvent(): Observable<Any>

  fun nextClickEvent(): Observable<Any>

}
