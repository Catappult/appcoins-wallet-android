package com.asfoundation.wallet.service;

import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ServiceErrorException;
import com.asfoundation.wallet.entity.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.tx.ChainId;

import static org.web3j.crypto.Wallet.create;

public class Web3jKeystoreAccountService implements AccountKeystoreService {
  private static final int PRIVATE_KEY_RADIX = 16;
  /**
   * CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than 2^(128 * r / 8).
   */
  private static final int N = 1 << 9;
  /**
   * Parallelization parameter. Must be a positive integer less than or equal to Integer.MAX_VALUE /
   * (128 * r * 8).
   */
  private static final int P = 1;

  private final KeyStoreFileManager keyStoreFileManager;
  private final String cacheFolder;
  private final KeyStoreFileManager cacheKeyStoreFileManager;
  private final Scheduler scheduler;

  public Web3jKeystoreAccountService(KeyStoreFileManager keyStoreFileManager, String cacheFolder,
      KeyStoreFileManager cacheKeyStoreFileManager, Scheduler scheduler) {
    this.keyStoreFileManager = keyStoreFileManager;
    this.cacheKeyStoreFileManager = cacheKeyStoreFileManager;
    if (cacheFolder.charAt(cacheFolder.length() - 1) == '/') {
      this.cacheFolder = cacheFolder;
    } else {
      this.cacheFolder = cacheFolder + "/";
    }
    this.scheduler = scheduler;
  }

  @Override public Single<Wallet> createAccount(String password) {
    return Single.fromCallable(() -> WalletUtils.generateNewWalletFile(password,
        new File(keyStoreFileManager.getKeystoreFolderPath()), false))
        .map(fileName -> new Wallet(extractAddressFromFileName(fileName)))
        .subscribeOn(scheduler);
  }

  @Override
  public Single<Wallet> importKeystore(String store, String password, String newPassword) {
    return Single.fromCallable(() -> extractAddressFromStore(store))
        .flatMap(address -> {
          if (hasAccount(address)) {
            return Single.error(
                new ServiceErrorException(C.ErrorCode.ALREADY_ADDED, "Already added"));
          } else {
            return importKeystoreInternal(store, password, newPassword);
          }
        })
        .subscribeOn(scheduler);
  }

  @Override public Single<Wallet> importPrivateKey(String privateKey, String newPassword) {
    return Single.fromCallable(() -> {
      BigInteger key = new BigInteger(privateKey, PRIVATE_KEY_RADIX);
      ECKeyPair keypair = ECKeyPair.create(key);
      WalletFile walletFile = create(newPassword, keypair, N, P);
      return new ObjectMapper().writeValueAsString(walletFile);
    })
        .flatMap(keystore -> importKeystore(keystore, newPassword, newPassword));
  }

  @Override
  public Single<String> exportAccount(Wallet wallet, String password, String newPassword) {
    return Single.fromCallable(() -> keyStoreFileManager.getKeystore(wallet.address))
        .map(keystoreFilePath -> WalletUtils.loadCredentials(password, keystoreFilePath))
        .map(credentials -> WalletUtils.generateWalletFile(newPassword, credentials.getEcKeyPair(),
            new File(cacheKeyStoreFileManager.getKeystoreFolderPath()), false))
        .map(file -> {
          String keystore = readKeystore(cacheKeyStoreFileManager.getKeystoreFolderPath() + file);
          cacheKeyStoreFileManager.delete(cacheKeyStoreFileManager.getKeystoreFolderPath() + file);
          return keystore;
        })
        .subscribeOn(scheduler);
  }

  @Override public Completable deleteAccount(String address, String password) {
    return exportAccount(new Wallet(address), password, password).doOnSuccess(
        __ -> keyStoreFileManager.delete(keyStoreFileManager.getKeystore(address)))
        .subscribeOn(scheduler)
        .ignoreElement();
  }

  @Override
  public Single<byte[]> signTransaction(String fromAddress, String signerPassword, String toAddress,
      BigDecimal amount, BigDecimal gasPrice, BigDecimal gasLimit, long nonce, byte[] data,
      long chainId) {
    return Single.fromCallable(() -> {
      RawTransaction transaction =
          RawTransaction.createTransaction(BigInteger.valueOf(nonce), gasPrice.toBigInteger(),
              gasLimit.toBigInteger(), toAddress, amount.toBigInteger(), Hex.toHexString(data));

      Credentials credentials =
          WalletUtils.loadCredentials(signerPassword, keyStoreFileManager.getKeystore(fromAddress));

      byte convertedChainId = getChainId(chainId);
      return convertedChainId == ChainId.NONE ? TransactionEncoder.signMessage(transaction,
          credentials) : TransactionEncoder.signMessage(transaction, convertedChainId, credentials);
    })
        .subscribeOn(scheduler);
  }

  @Override public boolean hasAccount(String address) {
    return keyStoreFileManager.hasAddress(address);
  }

  @Override public Single<Wallet[]> fetchAccounts() {
    return Single.fromCallable(() -> {
      List<String> accounts = keyStoreFileManager.getAccounts();
      int len = accounts.size();
      Wallet[] result = new Wallet[len];

      for (int i = 0; i < len; i++) {
        String account = accounts.get(i);
        result[i] = new Wallet(account.toLowerCase());
      }
      return result;
    })
        .subscribeOn(scheduler);
  }

  private Single<Credentials> loadCredentialsFromKeystore(String keystore, String password) {
    return Single.fromCallable(() -> {
      String filePath = keyStoreFileManager.saveKeyStoreFile(keystore, cacheFolder);
      Credentials credentials = WalletUtils.loadCredentials(password, filePath);
      deleteKeystoreFile(filePath);
      return credentials;
    });
  }

  private Single<Wallet> importKeystoreInternal(String store, String password, String newPassword) {
    return loadCredentialsFromKeystore(store, password).map(
        credentials -> cacheFolder + WalletUtils.generateWalletFile(newPassword,
            credentials.getEcKeyPair(), new File(cacheFolder), false))
        .map(keystoreFilePath -> {
          String keystore = readKeystore(keystoreFilePath);
          deleteKeystoreFile(keystoreFilePath);
          return keystore;
        })
        .doOnSuccess(keyStoreFileManager::saveKeyStoreFile)
        .map(keystore -> new Wallet(extractAddressFromStore(keystore)))
        .doOnError(throwable -> keyStoreFileManager.delete(extractAddressFromStore(store)));
  }

  private void deleteKeystoreFile(String keystoreFilePath) {
    if (!new File(keystoreFilePath).delete()) {
      System.out.println(
          "**WARNING** GethKeystoreAccountService: unable to delete generated keystore "
              + "from cache");
    }
  }

  private String readKeystore(String keystoreFilePath) throws IOException {
    FileInputStream fis = new FileInputStream(new File(keystoreFilePath));

    StringBuilder fileContent = new StringBuilder();
    byte[] buffer = new byte[1024];
    int n;
    while ((n = fis.read(buffer)) != -1) {
      fileContent.append(new String(buffer, 0, n));
    }
    return fileContent.toString();
  }

  private String extractAddressFromFileName(String fileName) {
    String[] split = fileName.split("--");
    return "0x".concat(split[split.length - 1].split("\\.")[0]);
  }

  private byte getChainId(long chainId) {
    if (chainId == 1) {
      return ChainId.MAINNET;
    } else if (chainId == 61) {
      return ChainId.ETHEREUM_CLASSIC_MAINNET;
    } else if (chainId == 42) {
      return ChainId.KOVAN;
    } else if (chainId == 3) {
      return ChainId.ROPSTEN;
    } else {
      return ChainId.NONE;
    }
  }

  private String extractAddressFromStore(String store) throws Exception {
    try {
      JSONObject jsonObject = new JSONObject(store);
      return "0x" + jsonObject.getString("address");
    } catch (JSONException ex) {
      throw new Exception("Invalid keystore: " + store);
    }
  }
}
