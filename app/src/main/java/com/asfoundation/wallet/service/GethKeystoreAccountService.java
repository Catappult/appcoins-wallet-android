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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Objects;
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
  private final String keystoreFolderPath;

  public GethKeystoreAccountService(File keyStoreFile) {
    keystoreFolderPath = keyStoreFile.getAbsolutePath();
    keyStore = new KeyStore(keystoreFolderPath, Geth.LightScryptN, Geth.LightScryptP);
  }

  @Override public Single<Wallet> createAccount(String password) {
    return Single.fromCallable(() -> new Wallet(keyStore.newAccount(password)
        .getAddress()
        .getHex()
        .toLowerCase()))
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
    return Single.fromCallable(() -> findAccount(wallet.address))
        .flatMap(account1 -> Single.fromCallable(
            () -> new String(keyStore.exportKey(account1, password, newPassword))))
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

      Credentials credentials = WalletUtils.loadCredentials(signerPassword,
          Objects.requireNonNull(getFilePath(fromAddress.substring(2), keystoreFolderPath),
              "Wallet with address: " + fromAddress + " not found"));

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

  private String extractAddressFromStore(String store) throws Exception {
    try {
      JSONObject jsonObject = new JSONObject(store);
      return "0x" + jsonObject.getString("address");
    } catch (JSONException ex) {
      throw new Exception("Invalid keystore");
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
