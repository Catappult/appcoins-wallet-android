package com.asfoundation.wallet.entity

import com.asfoundation.wallet.ui.iab.FiatValue

class GlobalBalance(val appcoinsBalance: Balance, val appcoinsFiatValue: FiatValue,
                    val creditsBalance: Balance, val creditsFiatValue: FiatValue,
                    val etherBalance: Balance, val etherFiatValue: FiatValue) {

  fun changesOccured(globalBalance1: GlobalBalance, globalBalance2: GlobalBalance): Boolean {
    return !(globalBalance1 == globalBalance2)
  }
}

