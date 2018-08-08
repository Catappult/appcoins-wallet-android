package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofSubmissionFeeData;
import com.asfoundation.wallet.poa.ProofWriter;
import com.asfoundation.wallet.poa.TransactionFactory;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

public class BlockChainWriter implements ProofWriter {
  private final Web3jProvider web3jProvider;
  private final TransactionFactory transactionFactory;
  private final WalletRepositoryType walletRepositoryType;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final GasSettingsRepositoryType gasSettingsRepository;
  private final BigDecimal registerPoaGasLimit;
  private final EthereumNetworkRepositoryType ethereumNetwork;

  public BlockChainWriter(Web3jProvider web3jProvider, TransactionFactory transactionFactory,
      WalletRepositoryType walletRepositoryType, FindDefaultWalletInteract defaultWalletInteract,
      GasSettingsRepositoryType gasSettingsRepository, BigDecimal registerPoaGasLimit,
      EthereumNetworkRepositoryType ethereumNetwork) {
    this.web3jProvider = web3jProvider;
    this.transactionFactory = transactionFactory;
    this.walletRepositoryType = walletRepositoryType;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsRepository = gasSettingsRepository;
    this.registerPoaGasLimit = registerPoaGasLimit;
    this.ethereumNetwork = ethereumNetwork;
  }

  @Override public Single<String> writeProof(Proof proof) {
    return transactionFactory.createTransaction(proof)
        .flatMap(this::sendTransaction);
  }

  @Override public Single<ProofSubmissionFeeData> hasEnoughFunds(int chainId) {
    return defaultWalletInteract.find()
        .flatMap(wallet -> walletRepositoryType.balanceInWei(wallet, chainId))
        .flatMap(balance -> gasSettingsRepository.getGasSettings(true, chainId)
            .map(gasSettings -> getFeeData(
                balance.compareTo(registerPoaGasLimit.multiply(gasSettings.gasPrice)) >= 1,
                gasSettings)))
        .onErrorResumeNext(throwable -> {
          if (throwable instanceof WalletNotFoundException) {
            return Single.just(
                new ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.NO_WALLET,
                    BigDecimal.ZERO, BigDecimal.ZERO));
          } else if (throwable instanceof UnknownHostException) {
            return Single.just(
                new ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.NO_NETWORK,
                    BigDecimal.ZERO, BigDecimal.ZERO));
          }
          return Single.error(throwable);
        });
  }

  private ProofSubmissionFeeData getFeeData(boolean hasFunds, GasSettings gasSettings) {
    if (hasFunds) {
      return new ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.READY,
          registerPoaGasLimit, gasSettings.gasPrice);
    }
    return new ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.NO_FUNDS,
        BigDecimal.ZERO, BigDecimal.ZERO);
  }

  private Single<String> sendTransaction(byte[] transaction) {
    return Single.fromCallable(() -> {
      EthSendTransaction sentTransaction = web3jProvider.getDefault()
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
