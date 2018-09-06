package com.asfoundation.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import org.web3j.crypto.WalletFile;

public class KeyStoreFileManager {
  private final String keystoreFolderPath;
  private final ObjectMapper mapper;

  public KeyStoreFileManager(String keystoreFolderPath, ObjectMapper mapper) {
    this.mapper = mapper;
    new File(keystoreFolderPath).mkdirs();
    if (keystoreFolderPath.charAt(keystoreFolderPath.length() - 1) == '/') {
      this.keystoreFolderPath = keystoreFolderPath;
    } else {
      this.keystoreFolderPath = keystoreFolderPath + "/";
    }
  }

  public String getKeystore(String accountAddress) {
    return Objects.requireNonNull(
        getFilePath(removeHexIndicator(accountAddress), keystoreFolderPath),
        "Wallet with address: " + accountAddress + " not found");
  }

  private String removeHexIndicator(String accountAddress) {
    return accountAddress.replace("0x", "");
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

  /**
   * @return keystore file's absolute path
   */
  public String saveKeyStoreFile(String keystore) throws IOException {
    WalletFile walletFile = mapper.readValue(keystore, WalletFile.class);
    File keystoreFile = new File(keystoreFolderPath.concat(getWalletFileName(walletFile)));
    mapper.writeValue(keystoreFile, walletFile);
    return keystoreFile.getAbsolutePath();
  }

  public boolean delete(String keystoreFilePath) {
    return new File(keystoreFilePath).delete();
  }

  private String getWalletFileName(WalletFile walletFile) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
    return dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
  }

  public boolean hasAddress(String address) {
    return getFilePath(removeHexIndicator(address), keystoreFolderPath) != null;
  }
}
