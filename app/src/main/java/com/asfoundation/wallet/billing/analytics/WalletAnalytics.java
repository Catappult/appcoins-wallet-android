package com.asfoundation.wallet.billing.analytics;

import androidx.annotation.Nullable;
import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.AnalyticsManager.Action;
import java.util.HashMap;
import java.util.Map;

public class WalletAnalytics implements WalletEventSender {

  public static final String ACTION_CREATE = "create";
  public static final String ACTION_BACK = "back";
  public static final String ACTION_SAVE = "save";
  public static final String ACTION_FINISH = "finish";
  public static final String ACTION_CANCEL = "cancel";
  public static final String ACTION_IMPORT = "import";
  public static final String ACTION_IMPORT_FROM_FILE = "import_from_file";
  public static final String CONTEXT_CARD = "card";
  public static final String CONTEXT_SETTINGS = "settings";
  public static final String STATUS_SUCCESS = "success";
  public static final String STATUS_FAIL = "fail";

  public static final String WALLET_CREATE_BACKUP = "wallet_create_backup";
  public static final String WALLET_SAVE_BACKUP = "wallet_save_backup";
  public static final String WALLET_CONFIRMATION_BACKUP = "wallet_confirmation_backup";
  public static final String WALLET_SAVE_FILE = "wallet_save_file";
  public static final String WALLET_IMPORT_RESTORE = "wallet_import_restore";
  public static final String WALLET_PASSWORD_RESTORE = "wallet_password_restore";
  public static final String WALLET_COMPLETE_RESTORE = "wallet_complete_restore";

  private static final String WALLET = "WALLET";
  private static final String EVENT_ACTION = "action";
  private static final String EVENT_CONTEXT = "context";
  private static final String EVENT_STATUS = "status";
  private static final String EVENT_ERROR_DETAILS = "errorDetails";

  private final AnalyticsManager analytics;

  public WalletAnalytics(AnalyticsManager analytics) {
    this.analytics = analytics;
  }

  @Override public void sendCreateBackupEvent(String action, String context, String status) {
    sendCreateBackupEvent(action, context, status, null);
  }

  @Override public void sendCreateBackupEvent(String action, String context, String status,
      @Nullable String errorDetails) {

    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_ACTION, action);
    eventData.put(EVENT_CONTEXT, context);
    eventData.put(EVENT_STATUS, status);
    if (errorDetails != null) {
      eventData.put(EVENT_ERROR_DETAILS, errorDetails);
    }

    analytics.logEvent(eventData, WALLET_CREATE_BACKUP, Action.CLICK, WALLET);
  }

  @Override public void sendSaveBackupEvent(String action) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_ACTION, action);

    analytics.logEvent(eventData, WALLET_SAVE_BACKUP, Action.CLICK, WALLET);
  }

  @Override public void sendWalletConfirmationBackupEvent(String action) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_ACTION, action);

    analytics.logEvent(eventData, WALLET_CONFIRMATION_BACKUP, Action.CLICK, WALLET);
  }

  @Override public void sendWalletSaveFileEvent(String action, String status) {
    sendWalletSaveFileEvent(action, status, null);
  }

  @Override public void sendWalletSaveFileEvent(String action, String status, String errorDetails) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_ACTION, action);
    eventData.put(EVENT_STATUS, status);
    if (errorDetails != null) {
      eventData.put(EVENT_ERROR_DETAILS, errorDetails);
    }

    analytics.logEvent(eventData, WALLET_SAVE_FILE, Action.CLICK, WALLET);
  }

  @Override public void sendWalletImportRestoreEvent(String action, String status) {
    sendWalletImportRestoreEvent(action, status, null);
  }

  @Override
  public void sendWalletImportRestoreEvent(String action, String status, String errorDetails) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_ACTION, action);
    eventData.put(EVENT_STATUS, status);
    if (errorDetails != null) {
      eventData.put(EVENT_ERROR_DETAILS, errorDetails);
    }

    analytics.logEvent(eventData, WALLET_IMPORT_RESTORE, Action.CLICK, WALLET);
  }

  @Override public void sendWalletPasswordRestoreEvent(String action, String status) {
    sendWalletPasswordRestoreEvent(action, status, null);
  }

  @Override
  public void sendWalletPasswordRestoreEvent(String action, String status, String errorDetails) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_ACTION, action);
    eventData.put(EVENT_STATUS, status);
    if (errorDetails != null) {
      eventData.put(EVENT_ERROR_DETAILS, errorDetails);
    }

    analytics.logEvent(eventData, WALLET_PASSWORD_RESTORE, Action.CLICK, WALLET);
  }

  @Override public void sendWalletCompleteRestoreEvent(String status) {
    sendWalletCompleteRestoreEvent(status, null);
  }

  @Override public void sendWalletCompleteRestoreEvent(String status, String errorDetails) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_STATUS, status);
    if (errorDetails != null) {
      eventData.put(EVENT_ERROR_DETAILS, errorDetails);
    }

    analytics.logEvent(eventData, WALLET_COMPLETE_RESTORE, Action.CLICK, WALLET);
  }
}
