package com.asfoundation.wallet.ui.iab.payments.carrier.status

interface CarrierPaymentView {
  fun setLoading()

  fun showFinishedTransaction()

  fun getFinishedDuration(): Long
}