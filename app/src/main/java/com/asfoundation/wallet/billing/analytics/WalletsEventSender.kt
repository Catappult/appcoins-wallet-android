package com.asfoundation.wallet.billing.analytics

interface WalletsEventSender {
  fun sendCreateBackupEvent(action: String, context: String,
                            status: String)

  fun sendSaveBackupEvent(action: String)
  fun sendWalletConfirmationBackupEvent(action: String)
  fun sendWalletSaveFileEvent(action: String, status: String)
  fun sendWalletSaveFileEvent(action: String, status: String,
                              errorDetails: String?)

  fun sendWalletImportRestoreEvent(action: String, status: String)
  fun sendWalletImportRestoreEvent(action: String, status: String,
                                   errorDetails: String?)

  fun sendWalletPasswordRestoreEvent(action: String,
                                     status: String)

  fun sendWalletPasswordRestoreEvent(action: String, status: String,
                                     errorDetails: String?)

  fun sendWalletCompleteRestoreEvent(status: String)
  fun sendWalletCompleteRestoreEvent(status: String,
                                     errorDetails: String?)
}