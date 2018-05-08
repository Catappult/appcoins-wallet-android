package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofWriter;
import com.asfoundation.wallet.poa.TransactionFactory;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

public class BlockChainWriter implements ProofWriter {
  private final Web3jProvider web3jProvider;
  private final TransactionFactory transactionFactory;
  private final WalletRepositoryType walletRepositoryType;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final GasSettingsRepositoryType gasSettingsRepository;
  private final BigDecimal registerPoaGasLimit;

  public BlockChainWriter(Web3jProvider web3jProvider, TransactionFactory transactionFactory,
      WalletRepositoryType walletRepositoryType, FindDefaultWalletInteract defaultWalletInteract,
      GasSettingsRepositoryType gasSettingsRepository, BigDecimal registerPoaGasLimit) {
    this.web3jProvider = web3jProvider;
    this.transactionFactory = transactionFactory;
    this.walletRepositoryType = walletRepositoryType;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsRepository = gasSettingsRepository;
    this.registerPoaGasLimit = registerPoaGasLimit;
  }

  @Override public Single<String> writeProof(Proof proof) {
    return transactionFactory.createTransaction(proof)
        .flatMap(this::sendTransaction);
  }

  @Override public Single<Boolean> hasEnoughFunds() {
    return defaultWalletInteract.find()
        .flatMap(walletRepositoryType::balanceInWei)
        .flatMap(balance -> gasSettingsRepository.getGasSettings(true)
            .map(
                gasSettings -> balance.compareTo(registerPoaGasLimit.multiply(gasSettings.gasPrice))
                    >= 1));
  }

  private Single<String> sendTransaction(byte[] transaction) {
    return Single.fromCallable(() -> {
      EthSendTransaction sentTransaction = web3jProvider.get()
          .ethSendRawTransaction(Numeric.toHexString(transaction))
          .send();
      if (sentTransaction.hasError()) {
        throw new TransactionException(sentTransaction.getError()
            .getCode(), sentTransaction.getError()
            .getMessage(), sentTransaction.getError()
            .getData());
      }
      return sentTransaction.getTransactionHash();
    });
  }
}
