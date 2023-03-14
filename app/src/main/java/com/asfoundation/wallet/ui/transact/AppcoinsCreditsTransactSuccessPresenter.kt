package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class AppcoinsCreditsTransactSuccessPresenter(private val view: AppcoinsCreditsTransactSuccessView,
                                              private val amount: BigDecimal,
                                              private val currency: String,
                                              private val toAddress: String,
                                              private val disposables: CompositeDisposable,
                                              private val formatter: CurrencyFormatUtils) {
  fun present() {
    val walletCurrency = mapToWalletCurrency(currency)
    val formattedAmount = formatter.formatTransferCurrency(amount, walletCurrency)
    view.setup(formattedAmount, walletCurrency.symbol, toAddress)
    handleOkButtonClick()
  }

  private fun handleOkButtonClick() {
    disposables.add(view.getOkClick()
        .doOnNext { view.close() }
        .subscribe())
  }

  fun stop() {
    disposables.clear()
  }

  private fun mapToWalletCurrency(currency: String): WalletCurrency {
    return when (currency) {
      "APPC" -> WalletCurrency.APPCOINS
      "APPC-C" -> WalletCurrency.CREDITS
      else -> WalletCurrency.ETHEREUM
    }
  }
}
