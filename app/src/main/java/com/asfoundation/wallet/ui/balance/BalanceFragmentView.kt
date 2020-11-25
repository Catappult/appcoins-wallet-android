package com.asfoundation.wallet.ui.balance

import android.view.View
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Observable

interface BalanceFragmentView {

  fun setupUI()

  fun updateTokenValue(tokenBalance: String,
                       fiatBalance: String,
                       tokenCurrency: WalletCurrency,
                       fiatCurrency: String)

  fun updateOverallBalance(overallBalance: String, currency: String, symbol: String)

  fun getCreditClick(): Observable<View>

  fun getAppcClick(): Observable<View>

  fun getEthClick(): Observable<View>

  fun showTokenDetails(view: View)

  fun getCopyClick(): Observable<Any>

  fun getQrCodeClick(): Observable<Any>

  fun setWalletAddress(walletAddress: String)

  fun setAddressToClipBoard(walletAddress: String)

  fun showQrCodeView()

  fun backPressed(): Observable<Any>

  fun handleBackPress()

  fun showWalletCreatedAnimation()

  fun showCreatingAnimation()

  fun changeBottomSheetState()

  fun getBackupClick(): Observable<Any>

  fun setTooltip()

  fun getTooltipDismissClick(): Observable<Any>

  fun getTooltipBackupButton(): Observable<Any>

  fun homeBackPressed(): Observable<Any>?

  fun dismissTooltip()

  fun getVerifyWalletClick(): Observable<Any>

  fun getInsertCodeClick(): Observable<Any>

  fun openWalletValidationScreen()

  fun openWalletVerificationScreen()

  fun showVerifiedWalletChip()

  fun hideVerifiedWalletChip()

  fun showUnverifiedWalletChip()

  fun hideUnverifiedWalletChip()

  fun showRequestedCodeWalletChip()

  fun hideRequestedCodeWalletChip()

  fun disableVerifyWalletButton()

  fun enableVerifyWalletButton()
}
