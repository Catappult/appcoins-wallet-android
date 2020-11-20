package com.asfoundation.wallet.ui.iab.payments.carrier.status

import java.math.BigDecimal

interface CarrierPaymentView {

  fun initializeView(bonusValue: BigDecimal?, currency: String)

  fun setLoading()

  fun showFinishedTransaction()

  fun getFinishedDuration(): Long
}