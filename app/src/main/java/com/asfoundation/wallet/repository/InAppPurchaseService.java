package com.asfoundation.wallet.repository;

import android.support.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 13/03/2018.
 */

public class InAppPurchaseService {

  private final Repository<String, PaymentTransaction> cache;
  private final ApproveService approveService;
  private final BuyService buyService;
  private final NonceGetter nonceGetter;
  private final BalanceService balanceService;

  public InAppPurchaseService(Repository<String, PaymentTransaction> cache,
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
            .flatMapCompletable(buyTransaction -> cache.get(buyTransaction.getKey())
                .firstOrError()
                .map(paymentTransaction -> map(paymentTransaction, buyTransaction))
                .flatMap(
                    paymentTransaction -> cache.save(buyTransaction.getKey(), paymentTransaction)
                        .toSingleDefault(paymentTransaction))
                .filter(transaction -> transaction.getState()
                    .equals(PaymentTransaction.PaymentState.BOUGHT))
                .flatMapCompletable(transaction -> cache.save(transaction.getUri(),
                    new PaymentTransaction(transaction,
                        PaymentTransaction.PaymentState.COMPLETED)))))
        .subscribe();
  }

  @NonNull private PaymentTransaction map(PaymentTransaction paymentTransaction,
      BuyService.BuyTransaction buyTransaction) {
    return new PaymentTransaction(paymentTransaction, getStatus(buyTransaction.getStatus()),
        paymentTransaction.getApproveHash(), buyTransaction.getTransactionHash());
  }

  private PaymentTransaction.PaymentState getStatus(BuyService.Status status) {
    PaymentTransaction.PaymentState paymentState;
    switch (status) {
      case BUYING:
        paymentState = PaymentTransaction.PaymentState.BUYING;
        break;
      case BOUGHT:
        paymentState = PaymentTransaction.PaymentState.BOUGHT;
        break;
      default:
      case ERROR:
        paymentState = PaymentTransaction.PaymentState.ERROR;
        break;
      case WRONG_NETWORK:
        paymentState = PaymentTransaction.PaymentState.WRONG_NETWORK;
        break;
      case NONCE_ERROR:
        paymentState = PaymentTransaction.PaymentState.NONCE_ERROR;
        break;
      case UNKNOWN_TOKEN:
        paymentState = PaymentTransaction.PaymentState.UNKNOWN_TOKEN;
        break;
      case NO_TOKENS:
        paymentState = PaymentTransaction.PaymentState.NO_TOKENS;
        break;
      case NO_ETHER:
        paymentState = PaymentTransaction.PaymentState.NO_ETHER;
        break;
      case NO_FUNDS:
        paymentState = PaymentTransaction.PaymentState.NO_FUNDS;
        break;
      case NO_INTERNET:
        paymentState = PaymentTransaction.PaymentState.NO_INTERNET;
        break;
      case PENDING:
        paymentState = PaymentTransaction.PaymentState.PENDING;
        break;
    }
    return paymentState;
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
