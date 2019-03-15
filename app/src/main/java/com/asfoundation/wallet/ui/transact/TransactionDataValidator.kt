package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.entity.Address
import java.math.BigDecimal

class TransactionDataValidator {
  fun validateData(toWallet: String, amount: BigDecimal, balance: BigDecimal): DataStatus {
    if (!Address.isAddress(toWallet)) {
      return DataStatus.INVALID_WALLET_ADDRESS
    }
    if (amount.compareTo(BigDecimal.ZERO) < 1) {
      return DataStatus.INVALID_AMOUNT
    }
    if (amount.compareTo(balance) == 1) {
      return DataStatus.NOT_ENOUGH_FUNDS
    }
    return DataStatus.OK
  }

  enum class DataStatus {
    OK, INVALID_AMOUNT, INVALID_WALLET_ADDRESS, NOT_ENOUGH_FUNDS
  }
}
