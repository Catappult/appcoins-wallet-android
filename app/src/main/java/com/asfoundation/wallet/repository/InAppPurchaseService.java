package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;

/**
 * Created by trinkes on 13/03/2018.
 */

public class InAppPurchaseService {

  public static final double GAS_PRICE_MULTIPLIER = 1.25;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final TransferParser parser;
  private final Cache<String, PaymentTransaction> cache;
  private final ApproveService approveService;
  private final BuyService buyService;
  private final NonceGetter nonceGetter;
  private final BalanceService balanceService;
  private final BigDecimal paymentGasLimit;

  public InAppPurchaseService(FetchGasSettingsInteract gasSettingsInteract,
      FindDefaultWalletInteract defaultWalletInteract, TransferParser parser,
      Cache<String, PaymentTransaction> cache, ApproveService approveService, BuyService buyService,
      NonceGetter nonceGetter, BalanceService balanceService, BigDecimal paymentGasLimit) {
    this.gasSettingsInteract = gasSettingsInteract;
    this.defaultWalletInteract = defaultWalletInteract;
    this.parser = parser;
    this.cache = cache;
    this.approveService = approveService;
    this.buyService = buyService;
    this.nonceGetter = nonceGetter;
    this.balanceService = balanceService;
    this.paymentGasLimit = paymentGasLimit;
  }

  public Completable send(String uri) {
    return buildPaymentTransaction(uri).doOnSuccess(
        paymentTransaction -> cache.saveSync(paymentTransaction.getUri(), paymentTransaction))
        .flatMapCompletable(paymentTransaction -> balanceService.hasEnoughBalance(
            paymentTransaction.getTransactionBuilder(), paymentGasLimit)
            .flatMapCompletable(balance -> {
              switch (balance) {
                case NO_TOKEN:
                  return cache.save(paymentTransaction.getUri(),
                      new PaymentTransaction(paymentTransaction,
                          PaymentTransaction.PaymentState.NO_TOKENS));
                case NO_ETHER:
                  return cache.save(paymentTransaction.getUri(),
                      new PaymentTransaction(paymentTransaction,
                          PaymentTransaction.PaymentState.NO_ETHER));
                case NO_ETHER_NO_TOKEN:
                  return cache.save(paymentTransaction.getUri(),
                      new PaymentTransaction(paymentTransaction,
                          PaymentTransaction.PaymentState.NO_FUNDS));
                case OK:
                default:
                  return cache.save(paymentTransaction.getUri(), paymentTransaction)
                      .andThen(nonceGetter.getNonce()
                          .flatMapCompletable(nonce -> approveService.approve(uri,
                              new PaymentTransaction(paymentTransaction, nonce))));
              }
            }));
  }

  private Single<PaymentTransaction> buildPaymentTransaction(String uri) {
    return Single.zip(parseTransaction(uri), defaultWalletInteract.find(),
        (transaction, wallet) -> transaction.fromAddress(wallet.address))
        .flatMap(transactionBuilder -> gasSettingsInteract.fetch(true)
            .map(gasSettings -> transactionBuilder.gasSettings(
                new GasSettings(gasSettings.gasPrice.multiply(new BigDecimal(GAS_PRICE_MULTIPLIER)),
                    paymentGasLimit.divide(new BigDecimal("2"), BigDecimal.ROUND_FLOOR)))))
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

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return parser.parse(uri);
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
