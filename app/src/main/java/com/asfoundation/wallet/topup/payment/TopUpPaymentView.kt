package com.asfoundation.wallet.topup.payment

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import java.io.IOException

interface TopUpPaymentView {

  fun setup(convertToFiatResponseBody: FiatValue)

  fun showError()

  fun close()

  fun hideLoading()

  fun showLoading()

  fun setupUiCompleted(): Observable<Boolean>

  @Throws(IOException::class)
  fun finish(purchase: Purchase)
}
