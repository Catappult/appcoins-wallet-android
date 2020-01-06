package com.asfoundation.wallet.interact

import com.asfoundation.wallet.util.ImportError

data class WalletModel(val address: String, val error: ImportError = ImportError()) {

  constructor(importError: ImportError) : this("", importError)
}
