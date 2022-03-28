package com.asfoundation.wallet.nfts.repository

import com.asfoundation.wallet.nfts.domain.FailedNftTransfer
import com.asfoundation.wallet.nfts.domain.NftTransferResult

class NftTransferErrorMapper {
  fun map(throwable: Throwable): NftTransferResult {
    return if (throwable.message != null) {
      when (throwable.message) {
        "already known" -> FailedNftTransfer.AlreadyKnown(throwable)
        "insufficient funds for gas * price + value" -> FailedNftTransfer.InsufficientFunds(
            throwable)
        "replacement transaction underpriced" -> FailedNftTransfer.ReplacementUnderpriced(throwable)
        "intrinsic gas too low" -> FailedNftTransfer.GasToLow(throwable)
        else -> FailedNftTransfer.GenericError(throwable)
      }
    } else {
      FailedNftTransfer.GenericError(throwable)
    }
  }
}