package com.asfoundation.wallet.ui

import com.asfoundation.wallet.entity.TransactionBuilder

internal interface Erc681ReceiverView {

  fun getCallingPackage(): String?

  fun startEipTransfer(transactionBuilder: TransactionBuilder, isBds: Boolean)

  fun startApp(throwable: Throwable)

  fun endAnimation()

  fun showLoadingAnimation()

  fun launchWebViewPayment(url: String, transaction: TransactionBuilder, type: String)
}
