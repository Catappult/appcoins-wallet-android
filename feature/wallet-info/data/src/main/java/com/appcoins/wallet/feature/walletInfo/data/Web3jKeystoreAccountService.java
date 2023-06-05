package com.appcoins.wallet.feature.walletInfo.data;

import com.appcoins.wallet.core.utils.jvm_common.C;
import com.appcoins.wallet.feature.walletInfo.data.authentication.ServiceErrorException;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.reactivex.Completable;
import io.reactivex.Single;
import it.czerwinski.android.hilt.annotations.BoundTo;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.inject.Inject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.tx.ChainId;
import org.web3j.utils.Numeric;

import static org.web3j.crypto.Wallet.create;

@BoundTo(supertype = AccountKeystoreService.class) public class Web3jKeystoreAccountService
    implements AccountKeystoreService {
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

  private final BigInteger maxPriorityFee = BigInteger.valueOf(1_500_000_000L);

  private final KeyStoreFileManager keyStoreFileManager;
  private final ObjectMapper objectMapper;

  public @Inject Web3jKeystoreAccountService(KeyStoreFileManager keyStoreFileManager,
      ObjectMapper objectMapper) {
    this.keyStoreFileManager = keyStoreFileManager;
    this.objectMapper = objectMapper;
  }

  @Override public Single<Wallet> createAccount(String password) {
    return Single.fromCallable(() -> WalletUtils.generateNewWalletFile(password,
        new File(keyStoreFileManager.getKeystoreFolderPath()), false))
        .map(fileName -> new Wallet(extractAddressFromFileName(fileName)));
  }

  @Override
  public Single<RestoreResult> restoreKeystore(String store, String password, String newPassword) {
    return Single.fromCallable(() -> extractAddressFromStore(store))
        .flatMap(address -> {
          if (hasAccount(address)) {
            return Single.error(
                new ServiceErrorException(C.ErrorCode.ALREADY_ADDED, "Already added"));
          } else {
            return importKeystoreInternal(store, password, newPassword);
          }
        })
        .map(wallet -> (RestoreResult) new SuccessfulRestore(wallet.getAddress()))
        .onErrorReturn(throwable -> new RestoreResultErrorMapper().map(throwable,
            extractAddressFromStore(store)));
  }

  @Override public Single<RestoreResult> restorePrivateKey(String privateKey, String newPassword) {
    return Single.fromCallable(() -> {
      BigInteger key = new BigInteger(privateKey, PRIVATE_KEY_RADIX);
      ECKeyPair keypair = ECKeyPair.create(key);
      WalletFile walletFile = create(newPassword, keypair, N, P);
      return new ObjectMapper().writeValueAsString(walletFile);
    })
        .flatMap(keystore -> restoreKeystore(keystore, newPassword, newPassword))
        .onErrorReturn(FailedRestore.InvalidPrivateKey::new);
  }

  @Override
  public Single<String> exportAccount(String address, String password, String newPassword) {
    return Single.fromCallable(() -> keyStoreFileManager.getKeystore(address))
        .map(keystoreFilePath -> WalletUtils.loadCredentials(password, keystoreFilePath))
        .map(credentials -> objectMapper.writeValueAsString(
            create(newPassword, credentials.getEcKeyPair(), N, P)));
  }

  @Override public Completable deleteAccount(String address, String password) {
    return exportAccount(address, password, password).doOnSuccess(
        __ -> keyStoreFileManager.delete(keyStoreFileManager.getKeystore(address)))
        .ignoreElement();
  }

  @Override
  public Single<byte[]> signTransaction(String fromAddress, String signerPassword, String toAddress,
      BigDecimal amount, BigDecimal gasPrice, BigDecimal gasLimit, long nonce, byte[] data,
      long chainId) {
    return Single.fromCallable(() -> {
      RawTransaction transaction;
      if (data == null) {
        transaction = RawTransaction.createEtherTransaction(
            chainId,
            BigInteger.valueOf(nonce),
            gasLimit.toBigInteger(),
            toAddress,
            amount.toBigInteger(),
            maxPriorityFee,           //maxPriorityFeePerGas
            gasPrice.toBigInteger()   //maxFeePerGas
        );
      } else {
        transaction = RawTransaction.createTransaction(
            chainId,
            BigInteger.valueOf(nonce),
            gasLimit.toBigInteger(),
            toAddress,
            amount.toBigInteger(),
            Numeric.toHexString(data),
            maxPriorityFee,             //maxPriorityFeePerGas
            gasPrice.toBigInteger()     //maxFeePerGas
        );
      }

      Credentials credentials =
          WalletUtils.loadCredentials(signerPassword, keyStoreFileManager.getKeystore(fromAddress));

      byte convertedChainId = getChainId(chainId);
      return convertedChainId == ChainId.NONE ? TransactionEncoder.signMessage(transaction,
          credentials) : TransactionEncoder.signMessage(transaction, convertedChainId, credentials);
    });
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
    });
  }

  private Single<Credentials> loadCredentialsFromKeystore(String keystore, String password) {
    return Single.fromCallable(() -> {
      WalletFile walletFile = objectMapper.readValue(keystore, WalletFile.class);
      return Credentials.create(org.web3j.crypto.Wallet.decrypt(password, walletFile));
    });
  }

  private Single<Wallet> importKeystoreInternal(String store, String password, String newPassword) {
    return loadCredentialsFromKeystore(store, password).map(credentials -> {
      WalletFile walletFile = create(newPassword, credentials.getEcKeyPair(), N, P);
      return objectMapper.writeValueAsString(walletFile);
    })
        .doOnSuccess(keyStoreFileManager::saveKeyStoreFile)
        .map(keystore -> new Wallet(extractAddressFromStore(keystore)))
        .doOnError(throwable -> keyStoreFileManager.delete(extractAddressFromStore(store)));
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

  private String extractAddressFromStore(String keystore) throws Exception {
    try {
      JsonObject keyStore = JsonParser.parseString(keystore)
          .getAsJsonObject();
      return "0x" + keyStore.get("address")
          .getAsString();
    } catch (Exception ex) {
      throw new Exception("Invalid keystore: " + keystore);
    }
  }
}
