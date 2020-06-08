package com.asfoundation.wallet.topup

interface LocalTopUpPaymentView {
  fun showValues(value: String, currency: String, appcValue: String)
}
