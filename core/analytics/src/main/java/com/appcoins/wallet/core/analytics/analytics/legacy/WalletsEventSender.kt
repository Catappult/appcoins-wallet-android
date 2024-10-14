package com.appcoins.wallet.core.analytics.analytics.legacy

interface WalletsEventSender {

  fun sendAction(action: String)

  fun sendVerifyAction(action: String, status: String)

  fun sendCreateBackupEvent(
    action: String?, context: String,
    status: String?
  )

  fun sendCreateBackupEvent(
    action: String?, context: String,
    status: String?, errorDetails: String? = null
  )

  fun sendBackupInfoEvent(action: String, option: String)

  fun sendBackupConfirmationEvent(action: String)

  fun sendBackupConclusionEvent(action: String)

  fun sendWalletSaveFileEvent(
    action: String, status: String,
    errorDetails: String? = null
  )

  fun sendWalletRestoreEvent(
    action: String, status: String,
    errorDetails: String? = null
  )

  fun sendWalletPasswordRestoreEvent(
    action: String,
    status: String
  )

  fun sendWalletPasswordRestoreEvent(
    action: String, status: String,
    errorDetails: String? = null
  )

  fun sendWalletCompleteRestoreEvent(
    status: String,
    errorDetails: String? = null
  )
}