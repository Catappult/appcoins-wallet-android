package com.asfoundation.wallet.repository;

import com.appcoins.wallet.core.utils.jvm_common.UnknownTokenException;
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletNotFoundException;
import java.net.UnknownHostException;
import javax.inject.Inject;

/**
 * Created by trinkes on 20/03/2018.
 */

public class BlockchainErrorMapper {

  public @Inject BlockchainErrorMapper() {
  }

  private static final String INSUFFICIENT_ERROR_MESSAGE =
      "insufficient funds for gas * price + value";
  private static final String NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low";

  public BlockchainError map(Throwable throwable) {
    if (throwable instanceof UnknownHostException) {
      return BlockchainError.NO_INTERNET;
    }
    if (throwable instanceof WrongNetworkException) {
      return BlockchainError.WRONG_NETWORK;
    }
    if (throwable instanceof TransactionNotFoundException) {
      return BlockchainError.TRANSACTION_NOT_FOUND;
    }
    if (throwable instanceof UnknownTokenException) {
      return BlockchainError.UNKNOWN_TOKEN;
    }
    if (throwable instanceof TransactionException) {
      String message = throwable.getMessage();
      if (INSUFFICIENT_ERROR_MESSAGE.equals(message)) return BlockchainError.NO_FUNDS;
      if (NONCE_TOO_LOW_ERROR_MESSAGE.equals(message)) return BlockchainError.NONCE_ERROR;
    }
    if (throwable instanceof WalletNotFoundException) {
      return BlockchainError.NO_WALLET;
    }
    return BlockchainError.INVALID_BLOCKCHAIN_ERROR;
  }

  public enum BlockchainError {
    WRONG_NETWORK, TRANSACTION_NOT_FOUND, UNKNOWN_TOKEN, NO_FUNDS, NONCE_ERROR, NO_INTERNET,
    INVALID_BLOCKCHAIN_ERROR, NO_WALLET
  }
}
