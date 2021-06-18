package com.asfoundation.wallet.billing.analytics

interface WalletsEventSender {

  fun sendAction(action: String)

  fun sendVerifyAction(action: String, status: String)

  fun sendCreateBackupEvent(action: String, context: String,
                            status: String)

  fun sendCreateBackupEvent(action: String, context: String,
                            status: String, errorDetails: String? = null)

  fun sendSaveBackupEvent(action: String)

  fun sendWalletConfirmationBackupEvent(action: String)

  fun sendWalletSaveFileEvent(action: String, status: String,
                              errorDetails: String? = null)

  fun sendWalletRestoreEvent(action: String, status: String,
                             errorDetails: String? = null)

  fun sendWalletPasswordRestoreEvent(action: String,
                                     status: String)

  fun sendWalletPasswordRestoreEvent(action: String, status: String,
                                     errorDetails: String? = null)

  fun sendWalletCompleteRestoreEvent(status: String,
                                     errorDetails: String? = null)
}