package com.asfoundation.wallet.wallets

import com.asfoundation.wallet.util.RestoreError

data class WalletModel(val address: String, val keystore: String = "",
                       val error: RestoreError = RestoreError()) {

  constructor(restoreError: RestoreError) : this("", "", restoreError)
  constructor(keystore: String, restoreError: RestoreError) : this("", keystore, restoreError)
}
