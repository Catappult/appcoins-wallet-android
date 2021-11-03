package com.asfoundation.wallet.my_wallets.main.list

import android.view.View
import com.asfoundation.wallet.ui.wallets.WalletBalance

sealed class WalletsListEvent {

  data class QrCodeClick(val view: View) : WalletsListEvent()

  data class ShareWalletClick(val walletAddress: String) : WalletsListEvent()
  data class CopyWalletClick(val walletAddress: String) : WalletsListEvent()

  data class TokenClick(val token: Token) : WalletsListEvent() {
    enum class Token { APPC, APPC_C, ETH }
  }

  object BackupClick : WalletsListEvent()

  object VerifyWalletClick : WalletsListEvent()

  object CreateNewWalletClick : WalletsListEvent()
  data class OtherWalletClick(val walletBalance: WalletBalance) : WalletsListEvent()

  data class ChangedBalanceVisibility(val balanceVisible: Boolean) : WalletsListEvent()
}