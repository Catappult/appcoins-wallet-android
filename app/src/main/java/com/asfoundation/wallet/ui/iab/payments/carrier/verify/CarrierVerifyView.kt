package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import android.graphics.drawable.Drawable
import io.reactivex.Observable
import java.math.BigDecimal

interface CarrierVerifyView {

  fun initializeView(appName: String, icon: Drawable,
                     currency: String, fiatAmount: BigDecimal,
                     appcAmount: BigDecimal, skuDescription: String,
                     bonusAmount: BigDecimal)

  fun backEvent(): Observable<Any>

  fun nextClickEvent(): Observable<Any>
}