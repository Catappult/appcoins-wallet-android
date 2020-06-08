package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class LocalTopUpPaymentPresenter(private val view: LocalTopUpPaymentFragment,
                                 private val activityView: TopUpActivityView,
                                 private val interactor: LocalPaymentInteractor,
                                 private val formatter: CurrencyFormatUtils,
                                 private val viewScheduler: Scheduler,
                                 private val networkScheduler: Scheduler,
                                 private val disposables: CompositeDisposable,
                                 private val data: TopUpPaymentData,
                                 private val paymentId: String) {

  fun present() {
    setupUi()
  }

  private fun setupUi() {
    val fiatAmount = formatter.formatCurrency(data.fiatValue, WalletCurrency.FIAT)
    val appcAmount = formatter.formatCurrency(data.appcValue, WalletCurrency.CREDITS)
    view.showValues(fiatAmount, data.fiatCurrencyCode, appcAmount)
  }

  fun stop() = disposables.clear()
}
