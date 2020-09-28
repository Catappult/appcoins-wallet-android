package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.util.WalletCurrency
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.Completable
import io.reactivex.Observable
import java.io.Serializable
import java.math.BigDecimal

interface TransferFragmentView {

  fun getSendClick(): Observable<TransferData>

  fun openEthConfirmationView(walletAddress: String, toWalletAddress: String,
                              amount: BigDecimal): Completable

  fun openAppcConfirmationView(walletAddress: String, toWalletAddress: String,
                               amount: BigDecimal): Completable

  fun openAppcCreditsConfirmationView(walletAddress: String, amount: BigDecimal,
                                      currency: Currency): Completable

  fun showLoading()

  fun hideLoading()

  fun showInvalidAmountError()

  fun showInvalidWalletAddress()

  fun showNotEnoughFunds()

  fun showUnknownError()

  fun getQrCodeButtonClick(): Observable<Any>

  fun showQrCodeScreen()

  fun getQrCodeResult(): Observable<Barcode>

  fun showAddress(address: String)

  fun getCurrencyChange(): Observable<Currency>

  fun showBalance(balance: String, currency: WalletCurrency)

  fun showWalletBlocked()

  fun showNoNetworkError()

  fun onAuthenticationResult(): Observable<Boolean>

  data class TransferData(val walletAddress: String, val currency: Currency,
                          val amount: BigDecimal) : Serializable

  enum class Currency {
    APPC_C, APPC, ETH
  }

  fun showCameraErrorToast()
}
