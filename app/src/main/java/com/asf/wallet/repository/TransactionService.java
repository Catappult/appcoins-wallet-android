package com.asf.wallet.repository;

import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.interact.FetchGasSettingsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
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
  private final Cache<String, PaymentTransaction> cache;
  private final ApproveService approveService;
  private final BuyService buyService;

  public TransactionService(FetchGasSettingsInteract gasSettingsInteract,
      SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService,
      FindDefaultWalletInteract defaultWalletInteract, TransferParser parser,
      Cache<String, PaymentTransaction> cache, ApproveService approveService,
      BuyService buyService) {
    this.gasSettingsInteract = gasSettingsInteract;
    this.sendTransactionInteract = sendTransactionInteract;
    this.pendingTransactionService = pendingTransactionService;
    this.defaultWalletInteract = defaultWalletInteract;
    this.parser = parser;
    this.cache = cache;
    this.approveService = approveService;
    this.buyService = buyService;
  }

  public Completable send(String uri) {
    return buildPaymentTransaction(uri).flatMapCompletable(
        transaction -> approveService.approve(uri, transaction));
  }

  private Single<PaymentTransaction> buildPaymentTransaction(String uri) {
    return Single.zip(parseTransaction(uri), defaultWalletInteract.find(),
        (transaction, wallet) -> transaction.fromAddress(wallet.address))
        .flatMap(transactionBuilder -> gasSettingsInteract.fetch(true)
            .map(gasSettings -> transactionBuilder.gasSettings(
                new GasSettings(gasSettings.gasPrice, new BigDecimal(DEFAULT_GAS_LIMIT)))))
        .map(transactionBuilder -> new PaymentTransaction(uri, transactionBuilder,
            PaymentTransaction.PaymentState.PENDING));
  }

  public void start() {
    approveService.start();
    buyService.start();
    approveService.getAll()
        .flatMapCompletable(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .flatMapCompletable(
                paymentTransaction -> cache.save(paymentTransaction.getUri(), paymentTransaction)
                    .toSingleDefault(paymentTransaction)
                    .filter(transaction -> transaction.getState()
                        .equals(PaymentTransaction.PaymentState.APPROVED))
                    .flatMapCompletable(
                        transaction -> buyService.buy(transaction.getUri(), transaction))))
        .subscribe();

    buyService.getAll()
        .flatMapCompletable(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .flatMapCompletable(
                paymentTransaction -> cache.save(paymentTransaction.getUri(), paymentTransaction)
                    .toSingleDefault(paymentTransaction)
                    .filter(transaction -> transaction.getState()
                        .equals(PaymentTransaction.PaymentState.BOUGHT))
                    .flatMapCompletable(transaction -> cache.save(transaction.getUri(),
                        new PaymentTransaction(paymentTransaction,
                            PaymentTransaction.PaymentState.COMPLETED)))))
        .subscribe();
  }

  public Observable<PendingTransaction> sendTransaction(String uri) {
    return Single.zip(parseTransaction(uri), defaultWalletInteract.find(),
        (transaction, wallet) -> transaction.fromAddress(wallet.address))
        .flatMapObservable(transactionBuilder -> gasSettingsInteract.fetch(true)
            .map(gasSettings -> transactionBuilder.gasSettings(
                new GasSettings(gasSettings.gasPrice, new BigDecimal(DEFAULT_GAS_LIMIT))))
            .flatMapObservable(this::send))
        .startWith(new PendingTransaction(null, true));
  }

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return parser.parse(uri);
  }

  private Observable<PendingTransaction> send(TransactionBuilder transaction) {
    return approve(transaction).concatMap(pendingTransaction -> {
      if (pendingTransaction.isPending()) {
        return Observable.just(pendingTransaction);
      }
      return buy(transaction);
    });
  }

  private Observable<PendingTransaction> buy(TransactionBuilder transaction) {
    return sendTransactionInteract.buy(transaction)
        .flatMapObservable(pendingTransactionService::checkTransactionState);
  }

  private Observable<PendingTransaction> approve(TransactionBuilder transaction) {
    return sendTransactionInteract.approve(transaction)
        .flatMapObservable(pendingTransactionService::checkTransactionState);
  }

  public Observable<PaymentTransaction> getTransactionState(String uri) {
    return cache.get(uri);
  }

  public Completable remove(String uriString) {
    return buyService.remove(uriString)
        .andThen(approveService.remove(uriString))
        .andThen(cache.remove(uriString));
  }
}
