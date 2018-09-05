package com.asfoundation.wallet.service;

import java.io.File;
import java.util.Objects;

public class KeyStoreFileManager {
  private final String keystoreFolderPath;

  public KeyStoreFileManager(String keystoreFolderPath) {
    new File(keystoreFolderPath).mkdirs();
    this.keystoreFolderPath = keystoreFolderPath;
  }

  public String getKeystore(String accountAddress) {
    return Objects.requireNonNull(getFilePath(accountAddress.replace("0x", ""), keystoreFolderPath),
        "Wallet with address: " + accountAddress + " not found");
  }

  private String getFilePath(String accountAddress, String path) {
    File file = new File(path);
    if (!file.isDirectory()) {
      if (file.getName()
          .toLowerCase()
          .contains(accountAddress.toLowerCase())) {
        return file.getAbsolutePath();
      }
    }
    if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        String filePath = getFilePath(accountAddress, subFile.getPath());
        if (filePath != null) {
          return filePath;
        }
      }
    }
    return null;
  }

  public String getKeystoreFolderPath() {
    return keystoreFolderPath;
  }
}
