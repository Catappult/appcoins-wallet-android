package com.appcoins.wallet.feature.walletInfo.data;

import android.content.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import org.web3j.crypto.WalletFile;

public class KeyStoreFileManager {
  private final String keystoreFolderPath;
  private final ObjectMapper mapper;

  public @Inject KeyStoreFileManager(@ApplicationContext Context context, ObjectMapper mapper) {
    File file = new File(context.getFilesDir(), "keystore/keystore");
    this.mapper = mapper;
    new File(file.getAbsolutePath()).mkdirs();
    if (file.getAbsolutePath()
        .charAt(file.getAbsolutePath()
            .length() - 1) == '/') {
      this.keystoreFolderPath = file.getAbsolutePath();
    } else {
      this.keystoreFolderPath = file.getAbsolutePath() + "/";
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
    if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        String filePath = getFilePath(accountAddress, subFile.getPath());
        if (filePath != null) {
          return filePath;
        }
      }
    } else {
      if (file.getName()
          .toLowerCase()
          .contains(accountAddress.toLowerCase())) {
        return file.getAbsolutePath();
      }
    }
    return null;
  }

  String getKeystoreFolderPath() {
    return keystoreFolderPath;
  }

  /**
   * @return keystore file's absolute path
   */
  private String saveKeyStoreFile(String keystore, String path) throws IOException {
    WalletFile walletFile = mapper.readValue(keystore, WalletFile.class);
    File keystoreFile = new File(path.concat(getWalletFileName(walletFile)));
    mapper.writeValue(keystoreFile, walletFile);
    return keystoreFile.getAbsolutePath();
  }

  /**
   * @return keystore file's absolute path
   */
  String saveKeyStoreFile(String keystore) throws IOException {
    return saveKeyStoreFile(keystore, keystoreFolderPath);
  }

  public boolean delete(String keystoreFilePath) {
    return new File(keystoreFilePath).delete();
  }

  private String getWalletFileName(WalletFile walletFile) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
    return dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
  }

  boolean hasAddress(String address) {
    return getFilePath(removeHexIndicator(address), keystoreFolderPath) != null;
  }

  List<String> getAccounts() {
    List<String> addresses = new ArrayList<>();
    for (File file : new File(keystoreFolderPath).listFiles()) {
      String address = getAddressFromFileName(file.getName());
      if (address != null) {
        addresses.add(address);
      }
    }
    return addresses;
  }

  private String getAddressFromFileName(String fileName) {
    try {
      String[] split = fileName.split("--");
      return "0x" + split[split.length - 1].split("\\.")[0];
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
