package com.asfoundation.wallet.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;

public class WalletUtils {
  public static Credentials loadCredentials(String password, String json)
      throws IOException, CipherException {
    ObjectMapper objectMapper = new ObjectMapper();

    WalletFile walletFile = objectMapper.readValue(json, WalletFile.class);
    return Credentials.create(Wallet.decrypt(password, walletFile));
  }
}
