package com.asfoundation.wallet.ui.transact

import io.reactivex.Completable
import io.reactivex.Observable
import java.math.BigDecimal

interface TransactFragmentView {
  fun getSendClick(): Observable<TransactData>
  fun openEthConfirmationView(walletAddress: String, toWalletAddress: String,
                              amount: BigDecimal): Completable

  fun openAppcConfirmationView(walletAddress: String, toWalletAddress: String,
                               amount: BigDecimal): Completable

  data class TransactData(val walletAddress: String, val currency: Currency, val amount: BigDecimal)

  enum class Currency {
    APPC_C, APPC, ETH
  }
}
