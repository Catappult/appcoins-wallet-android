package com.asfoundation.wallet.interact

import com.asfoundation.wallet.util.ImportError

data class WalletModel(val address: String, val keystore: String = "",
                       val error: ImportError = ImportError()) {

  constructor(importError: ImportError) : this("", "", importError)
  constructor(keystore: String, importError: ImportError) : this("", keystore, importError)
}
