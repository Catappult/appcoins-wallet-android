package com.appcoins.wallet.feature.backup.data.result

sealed class BackupResult

object SuccessfulBackup : BackupResult()

sealed class FailedBackup : BackupResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedBackup()
 // data class InvalidEmail(val throwable: Throwable? = null) : FailedBackup()
  //data class EmailNotExist(val throwable: Throwable? = null) : FailedBackup()
}



  /*
  class BackupMapper {

  fun map(walletAddress: EmailToBackup): BackupResult {
    return when (walletAddress.validity) {
      ValidityEmailState.VALID -> SuccessfulBackup(walletAddress)
      ValidityEmailState.INVALID -> FailedBackup.GenericError()
      ValidityEmailState.UNKNOWN -> FailedBackup.GenericError()
      else -> FailedBackup.GenericError()
    }
  }
}
   */
