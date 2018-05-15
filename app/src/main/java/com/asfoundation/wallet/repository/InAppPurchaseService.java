package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 13/03/2018.
 */

public class InAppPurchaseService {

  private final Cache<String, PaymentTransaction> cache;
  private final ApproveService approveService;
  private final BuyService buyService;
  private final NonceGetter nonceGetter;
  private final BalanceService balanceService;

  public InAppPurchaseService(Cache<String, PaymentTransaction> cache,
      ApproveService approveService, BuyService buyService, NonceGetter nonceGetter,
      BalanceService balanceService) {
    this.cache = cache;
    this.approveService = approveService;
    this.buyService = buyService;
    this.nonceGetter = nonceGetter;
    this.balanceService = balanceService;
  }

  public Completable send(String key, PaymentTransaction paymentTransaction) {
    return Completable.fromAction(() -> cache.saveSync(key, paymentTransaction))
        .andThen(balanceService.hasEnoughBalance(paymentTransaction.getTransactionBuilder(),
            paymentTransaction.getTransactionBuilder()
                .gasSettings().gasLimit)
            .flatMapCompletable(balance -> {
              switch (balance) {
                case NO_TOKEN:
                  return cache.save(key, new PaymentTransaction(paymentTransaction,
                      PaymentTransaction.PaymentState.NO_TOKENS));
                case NO_ETHER:
                  return cache.save(key, new PaymentTransaction(paymentTransaction,
                      PaymentTransaction.PaymentState.NO_ETHER));
                case NO_ETHER_NO_TOKEN:
                  return cache.save(key, new PaymentTransaction(paymentTransaction,
                      PaymentTransaction.PaymentState.NO_FUNDS));
                case OK:
                default:
                  return cache.save(key, paymentTransaction)
                      .andThen(nonceGetter.getNonce()
                          .flatMapCompletable(nonce -> approveService.approve(key,
                              new PaymentTransaction(paymentTransaction, nonce))));
              }
            }));
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

  public Observable<PaymentTransaction> getTransactionState(String key) {
    return cache.get(key);
  }

  public Completable remove(String key) {
    return buyService.remove(key)
        .andThen(approveService.remove(key))
        .andThen(cache.remove(key));
  }

  public Observable<List<PaymentTransaction>> getAll() {
    return cache.getAll();
  }
}
