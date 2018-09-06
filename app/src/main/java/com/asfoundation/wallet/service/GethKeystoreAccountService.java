package com.asfoundation.wallet.service;

import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ServiceErrorException;
import com.asfoundation.wallet.entity.ServiceException;
import com.asfoundation.wallet.entity.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Address;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
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

public class GethKeystoreAccountService implements AccountKeystoreService {
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

  private final KeyStore keyStore;
  private final KeyStoreFileManager keyStoreFileManager;
  private final String cacheFolder;

  public GethKeystoreAccountService(File keyStoreFile, KeyStoreFileManager keyStoreFileManager,
      String cacheFolder) {
    this.keyStoreFileManager = keyStoreFileManager;
    keyStore = new KeyStore(keyStoreFile.getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
    this.cacheFolder = cacheFolder;
  }

  @Override public Single<Wallet> createAccount(String password) {
    return Single.fromCallable(() -> WalletUtils.generateNewWalletFile(password,
        new File(keyStoreFileManager.getKeystoreFolderPath()), false))
        .map(fileName -> new Wallet(extractAddressFromFileName(fileName)))
        .subscribeOn(Schedulers.io());
  }

  @Override
  public Single<Wallet> importKeystore(String store, String password, String newPassword) {
    return Single.fromCallable(() -> {
      String address = extractAddressFromStore(store);
      if (hasAccount(address)) {
        throw new ServiceErrorException(C.ErrorCode.ALREADY_ADDED, "Already added");
      }
      Account account;
      try {
        account =
            keyStore.importKey(store.getBytes(Charset.forName("UTF-8")), password, newPassword);
      } catch (Exception ex) {
        // We need to make sure that we do not have a broken account
        deleteAccount(address, newPassword).subscribe(() -> {
        }, t -> {
        });
        throw ex;
      }
      return new Wallet(account.getAddress()
          .getHex()
          .toLowerCase());
    })
        .subscribeOn(Schedulers.io());
  }

  @Override public Single<Wallet> importPrivateKey(String privateKey, String newPassword) {
    return Single.fromCallable(() -> {
      BigInteger key = new BigInteger(privateKey, PRIVATE_KEY_RADIX);
      ECKeyPair keypair = ECKeyPair.create(key);
      WalletFile walletFile = create(newPassword, keypair, N, P);
      return new ObjectMapper().writeValueAsString(walletFile);
    })
        .compose(upstream -> importKeystore(upstream.blockingGet(), newPassword, newPassword));
  }

  @Override
  public Single<String> exportAccount(Wallet wallet, String password, String newPassword) {
    return Single.fromCallable(() -> keyStoreFileManager.getKeystore(wallet.address))
        .map(keystoreFilePath -> WalletUtils.loadCredentials(password, keystoreFilePath))
        .map(credentials -> WalletUtils.generateWalletFile(newPassword, credentials.getEcKeyPair(),
            new File(cacheFolder), false))
        .flatMap(fileName -> Single.just(readKeystore(cacheFolder.concat("/" + fileName)))
            .doOnSuccess(__ -> {
              if (!new File(cacheFolder.concat("/" + fileName)).delete()) {
                System.out.println(
                    "**WARNUNG** GethKeystoreAccountService: unable to delete generated keystore "
                        + "from cache");
              }
            }))
        .subscribeOn(Schedulers.io());
  }

  @Override public Completable deleteAccount(String address, String password) {
    return Single.fromCallable(() -> findAccount(address))
        .flatMapCompletable(
            account -> Completable.fromAction(() -> keyStore.deleteAccount(account, password)))
        .subscribeOn(Schedulers.io());
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
        .subscribeOn(Schedulers.io());
  }

  @Override public boolean hasAccount(String address) {
    return keyStore.hasAddress(new Address(address));
  }

  @Override public Single<Wallet[]> fetchAccounts() {
    return Single.fromCallable(() -> {
      Accounts accounts = keyStore.getAccounts();
      int len = (int) accounts.size();
      Wallet[] result = new Wallet[len];

      for (int i = 0; i < len; i++) {
        org.ethereum.geth.Account gethAccount = accounts.get(i);
        result[i] = new Wallet(gethAccount.getAddress()
            .getHex()
            .toLowerCase());
      }
      return result;
    })
        .subscribeOn(Schedulers.io());
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

  private org.ethereum.geth.Account findAccount(String address) throws ServiceException {
    Accounts accounts = keyStore.getAccounts();
    int len = (int) accounts.size();
    for (int i = 0; i < len; i++) {
      try {
        android.util.Log.d("ACCOUNT_FIND", "Address: " + accounts.get(i)
            .getAddress()
            .getHex());
        if (accounts.get(i)
            .getAddress()
            .getHex()
            .equalsIgnoreCase(address)) {
          return accounts.get(i);
        }
      } catch (Exception ex) {
        /* Quietly: interest only result, maybe next is ok. */
      }
    }
    throw new ServiceException("Wallet with address: " + address + " not found");
  }
}
