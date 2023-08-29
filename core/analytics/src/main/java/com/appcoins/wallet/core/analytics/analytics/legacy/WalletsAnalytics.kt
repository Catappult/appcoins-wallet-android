package com.appcoins.wallet.core.analytics.analytics.legacy

import cm.aptoide.analytics.AnalyticsManager
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = WalletsEventSender::class)
class WalletsAnalytics @Inject constructor(private val analytics: AnalyticsManager) :
    WalletsEventSender {

  override fun sendAction(action: String) {
    val data = HashMap<String, Any>()
    data[CALL_TO_ACTION] = action
    analytics.logEvent(
      data, WALLET_MY_WALLETS_INTERACTION_EVENT, AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendVerifyAction(action: String, status: String) {
    val data = HashMap<String, Any>()
    data[CALL_TO_ACTION] = action
    data[STATUS] = status
    analytics.logEvent(
      data, WALLET_MY_WALLETS_INTERACTION_EVENT, AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendCreateBackupEvent(
    action: String?, context: String,
    status: String?
  ) {
    sendCreateBackupEvent(action, context, status, null)
  }

  override fun sendCreateBackupEvent(
    action: String?, context: String,
    status: String?,
    errorDetails: String?
  ) {
    val eventData = HashMap<String, Any>()
    action?.let { eventData[EVENT_ACTION] = action }
    eventData[EVENT_CONTEXT] = context
    status?.let { eventData[EVENT_STATUS] = status }
    if (errorDetails != null) eventData[EVENT_ERROR_DETAILS] = errorDetails
    analytics.logEvent(eventData, WALLET_BACKUP_CREATE, AnalyticsManager.Action.CLICK, WALLET)
  }

//  override fun sendSaveBackupEvent(action: String) {
//    val eventData: MutableMap<String, Any> =
//      HashMap()
//    eventData[EVENT_ACTION] = action
//    analytics.logEvent(eventData, WALLET_SAVE_BACKUP, AnalyticsManager.Action.CLICK, WALLET)
//  }

  override fun sendBackupInfoEvent(action: String, option: String) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_ACTION] = action
    eventData[EVENT_OPTION] = option
    analytics.logEvent(eventData, WALLET_BACKUP_INFO, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendBackupConfirmationEvent(action: String) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_ACTION] = action
    analytics.logEvent(eventData, WALLET_BACKUP_CONFIRMATION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendBackupConclusionEvent(action: String) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_ACTION] = action
    analytics.logEvent(eventData, WALLET_BACKUP_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendWalletSaveFileEvent(
    action: String, status: String,
    errorDetails: String?
  ) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_ACTION] = action
    eventData[EVENT_STATUS] = status
    if (errorDetails != null) eventData[EVENT_ERROR_DETAILS] = errorDetails
    analytics.logEvent(
      eventData, WALLET_BACKUP_CONFIRMATION,
      AnalyticsManager.Action.CLICK, WALLET
    )
  }

  override fun sendWalletRestoreEvent(
    action: String, status: String,
    errorDetails: String?
  ) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_ACTION] = action
    eventData[EVENT_STATUS] = status
    if (errorDetails != null) eventData[EVENT_ERROR_DETAILS] = errorDetails
    analytics.logEvent(
      eventData, WALLET_IMPORT_RESTORE,
      AnalyticsManager.Action.CLICK, WALLET
    )
  }

  override fun sendWalletPasswordRestoreEvent(
    action: String,
    status: String
  ) {
    sendWalletPasswordRestoreEvent(action, status, null)
  }

  override fun sendWalletPasswordRestoreEvent(
    action: String, status: String,
    errorDetails: String?
  ) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_ACTION] = action
    eventData[EVENT_STATUS] = status
    if (errorDetails != null) eventData[EVENT_ERROR_DETAILS] = errorDetails
    analytics.logEvent(
      eventData, WALLET_PASSWORD_RESTORE,
      AnalyticsManager.Action.CLICK, WALLET
    )
  }

  override fun sendWalletCompleteRestoreEvent(
    status: String,
    errorDetails: String?
  ) {
    val eventData: MutableMap<String, Any> =
      HashMap()
    eventData[EVENT_STATUS] = status
    if (errorDetails != null) eventData[EVENT_ERROR_DETAILS] = errorDetails
    analytics.logEvent(
      eventData, WALLET_COMPLETE_RESTORE,
      AnalyticsManager.Action.CLICK, WALLET
    )
  }

  companion object {
    const val ACTION_CREATE = "create"
    const val ACTION_IMPORT = "import"
    const val ACTION_IMPORT_FROM_FILE = "import_from_file"
    const val ACTION_NEXT = "backup"
    const val ACTION_SEND_EMAIL = "send_to_email"
    const val ACTION_SAVE = "save"
    const val ACTION_UNDERSTAND = "understand"
    const val BACKUP_TRIGGER = "backup_trigger"
    const val SETTINGS = "settings"
    const val MY_WALLETS = "mywallets"
    const val OVERFLOW = "overflow"
    const val PASSWORD = "password"
    const val NO_PASSWORD = "no_password"
    const val CONTEXT_CARD = "card"
    const val CONTEXT_WALLET_DETAILS = "wallet_details"
    const val CONTEXT_WALLET_SETTINGS = "settings"
    const val STATUS_SUCCESS = "success"
    const val STATUS_FAIL = "fail"
    const val WALLET_BACKUP_CREATE = "wallet_backup_create"
    const val WALLET_BACKUP_INFO = "wallet_backup_info"
    const val WALLET_BACKUP_CONFIRMATION = "wallet_backup_confirmation"
    const val WALLET_BACKUP_CONCLUSION = "wallet_backup_conclusion"
    const val WALLET_IMPORT_RESTORE = "wallet_import_restore"
    const val WALLET_PASSWORD_RESTORE = "wallet_password_restore"
    const val WALLET_COMPLETE_RESTORE = "wallet_complete_restore"
    const val WALLET_MY_WALLETS_INTERACTION_EVENT = "wallet_my_wallets_interaction_event"
    const val CALL_TO_ACTION = "call_to_action"
    const val STATUS = "status"
    private const val WALLET = "WALLET"
    private const val EVENT_ACTION = "action"
    private const val EVENT_CONTEXT = "context"
    private const val EVENT_STATUS = "status"
    private const val EVENT_OPTION = "options"
    private const val EVENT_ERROR_DETAILS = "errorDetails"
  }

}