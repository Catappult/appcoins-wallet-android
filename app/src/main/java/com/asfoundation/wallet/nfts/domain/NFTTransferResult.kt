package com.asfoundation.wallet.nfts.domain

sealed class NftTransferResult()

data class SuccessfulNftTransfer(val throwable: Throwable? = null) : NftTransferResult()

sealed class FailedNftTransfer : NftTransferResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedNftTransfer()
  data class AlreadyKnown(val throwable: Throwable? = null) : FailedNftTransfer()
  data class InsufficientFunds(val throwable: Throwable? = null) : FailedNftTransfer()
  data class ReplacementUnderpriced(val throwable: Throwable? = null) : FailedNftTransfer()
  data class GasToLow(val throwable: Throwable? = null) : FailedNftTransfer()
}
