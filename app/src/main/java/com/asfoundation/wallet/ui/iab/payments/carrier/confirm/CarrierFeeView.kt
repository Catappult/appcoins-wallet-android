package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import android.graphics.drawable.Drawable
import io.reactivex.Observable
import java.math.BigDecimal

interface CarrierFeeView {

  fun initializeView(appName: String, appIcon: Drawable,
                     currency: String, fiatAmount: BigDecimal,
                     appcAmount: BigDecimal, skuDescription: String,
                     bonusAmount: BigDecimal, carrierName: String, carrierImage: String,
                     carrierFeeFiat: BigDecimal)

  fun backEvent(): Observable<Any>

  fun nextClickEvent(): Observable<Any>

}
