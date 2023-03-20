package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.Observable
import java.io.Serializable
import java.math.BigDecimal

interface TransferFragmentView {

  fun getSendClick(): Observable<TransferData>

  fun showInvalidAmountError()

  fun showInvalidWalletAddress()

  fun showNotEnoughFunds()

  fun showUnknownError()

  fun getQrCodeButtonClick(): Observable<Any>

  fun getQrCodeResult(): Observable<Barcode>

  fun showAddress(address: String)

  fun getCurrencyChange(): Observable<Currency>

  fun showBalance(balance: String, currency: WalletCurrency)

  fun showNoNetworkError()

  fun hideKeyboard()

  fun lockOrientation()

  fun unlockOrientation()

  data class TransferData(val walletAddress: String, val currency: Currency,
                          val amount: BigDecimal) : Serializable

  enum class Currency {
    APPC_C, APPC, ETH
  }

  fun showCameraErrorToast()
}
