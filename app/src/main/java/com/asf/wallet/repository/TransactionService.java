package com.asf.wallet.repository;

import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.interact.FetchGasSettingsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.util.TransferParser;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import java.math.BigDecimal;

/**
 * Created by trinkes on 13/03/2018.
 */

public class TransactionService {

  private static final String DEFAULT_GAS_LIMIT = "200000";
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final SendTransactionInteract sendTransactionInteract;
  private final PendingTransactionService pendingTransactionService;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final TransferParser parser;

  public TransactionService(FetchGasSettingsInteract gasSettingsInteract,
      SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService,
      FindDefaultWalletInteract defaultWalletInteract, TransferParser parser) {
    this.gasSettingsInteract = gasSettingsInteract;
    this.sendTransactionInteract = sendTransactionInteract;
    this.pendingTransactionService = pendingTransactionService;
    this.defaultWalletInteract = defaultWalletInteract;
    this.parser = parser;
  }

  public ObservableSource<PendingTransaction> sendTransaction(String uri) {
    return Single.zip(parser.parse(uri), defaultWalletInteract.find(),
        (transaction, wallet) -> transaction.fromAddress(wallet.address))
        .flatMap(transactionBuilder -> gasSettingsInteract.fetch(true)
            .map(gasSettings -> transactionBuilder.gasSettings(
                new GasSettings(gasSettings.gasPrice, new BigDecimal(DEFAULT_GAS_LIMIT)))))
        .flatMapObservable(transactionBuilder -> sendTransactionInteract.approve(transactionBuilder)
            .flatMapObservable(pendingTransactionService::checkTransactionState)
            .filter(pendingTransaction -> !pendingTransaction.isPending())
            .flatMapSingle(approved -> sendTransactionInteract.buy(transactionBuilder))
            .flatMap(pendingTransactionService::checkTransactionState)
            .startWith(new PendingTransaction(null, true)));
  }
}
