package com.asfoundation.wallet.billing.analytics;

import androidx.annotation.Nullable;

public interface WalletEventSender {

  void sendCreateBackupEvent(String action, String context, String status);

  void sendCreateBackupEvent(String action, String context, String status,
      @Nullable String errorDetails);

  void sendSaveBackupEvent(String action);

  void sendWalletConfirmationBackupEvent(String action);

  void sendWalletSaveFileEvent(String action, String status);

  void sendWalletSaveFileEvent(String action, String status, String errorDetails);

  void sendWalletImportRestoreEvent(String action, String status);

  void sendWalletImportRestoreEvent(String action, String status, String errorDetails);

  void sendWalletPasswordRestoreEvent(String action, String status);

  void sendWalletPasswordRestoreEvent(String action, String status, String errorDetails);

  void sendWalletCompleteRestoreEvent(String status, String errorDetails);
}
