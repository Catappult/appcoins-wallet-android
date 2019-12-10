package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.balance.BalanceScreenModel

interface WalletDetailView {
  fun populateUi(balanceScreenModel: BalanceScreenModel)
}
