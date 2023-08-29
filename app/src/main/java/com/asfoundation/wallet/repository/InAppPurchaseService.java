package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.appcoins.wallet.core.utils.jvm_common.Repository;
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.HasEnoughBalanceUseCase;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.repository.ApproveService.Status;
import com.appcoins.wallet.core.utils.jvm_common.UnknownTokenException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.List;
import org.web3j.utils.Convert;

import static com.asfoundation.wallet.repository.InAppPurchaseService.BalanceState.OK;

/**
 * Created by trinkes on 13/03/2018.
 */

public class InAppPurchaseService {

  private final Repository<String, PaymentTransaction> cache;
  private final ApproveService approveService;
  private final AllowanceService allowanceService;
  private final BuyService buyService;
  private final Scheduler scheduler;
  private final PaymentErrorMapper paymentErrorMapper;
  private final HasEnoughBalanceUseCase hasEnoughBalanceUseCase;
  private final DefaultTokenProvider defaultTokenProvider;

  public InAppPurchaseService(Repository<String, PaymentTransaction> cache,
      ApproveService approveService, AllowanceService allowanceService, BuyService buyService,
      Scheduler scheduler, PaymentErrorMapper paymentErrorMapper,
      HasEnoughBalanceUseCase hasEnoughBalanceUseCase, DefaultTokenProvider defaultTokenProvider) {
    this.cache = cache;
    this.approveService = approveService;
    this.allowanceService = allowanceService;
    this.buyService = buyService;
    this.scheduler = scheduler;
    this.paymentErrorMapper = paymentErrorMapper;
    this.hasEnoughBalanceUseCase = hasEnoughBalanceUseCase;
    this.defaultTokenProvider = defaultTokenProvider;
  }

  public Completable send(String key, PaymentTransaction paymentTransaction) {
    return checkFunds(key, paymentTransaction, checkAllowance(key, paymentTransaction));
  }

  private Completable checkAllowance(String key, PaymentTransaction paymentTransaction) {
    TransactionBuilder transactionBuilder = paymentTransaction.getTransactionBuilder();
    String fromAddress = transactionBuilder.fromAddress();
    String contractAddress = transactionBuilder.getIabContract();
    String tokenAddress = transactionBuilder.contractAddress();

    return allowanceService.checkAllowance(fromAddress, contractAddress, tokenAddress)
        .flatMapCompletable(allowance -> {

          if (allowance.compareTo(BigDecimal.ZERO) == 0) {
            return approveService.approve(key, paymentTransaction);
          } else {
            PaymentTransaction approveWithZeroPaymentTransaction =
                createApproveZeroTransaction(paymentTransaction);

            return approveService.approveWithoutValidation(key + "zero",
                approveWithZeroPaymentTransaction.getTransactionBuilder())
                .andThen(approveService.getApprove(key + "zero")
                    .filter(approveTransaction -> approveTransaction.getStatus() == Status.APPROVED)
                    .take(1)
                    .ignoreElements())
                .andThen(approveService.approve(key, paymentTransaction));
          }
        });
  }

  private PaymentTransaction createApproveZeroTransaction(PaymentTransaction paymentTransaction) {
    TransactionBuilder transactionBuilder = paymentTransaction.getTransactionBuilder();

    TransactionBuilder approveWithZeroTransactionBuilder =
        copyTransactionBuilder(transactionBuilder);
    approveWithZeroTransactionBuilder.amount(BigDecimal.ZERO);

    return new PaymentTransaction(paymentTransaction, approveWithZeroTransactionBuilder);
  }

  private TransactionBuilder copyTransactionBuilder(TransactionBuilder transactionBuilder) {
    return new TransactionBuilder(transactionBuilder);
  }

  public Completable resume(String key, PaymentTransaction paymentTransaction) {
    return checkFunds(key, paymentTransaction, buyService.buy(key, paymentTransaction));
  }

  private Completable checkFunds(String key, PaymentTransaction paymentTransaction,
      Completable action) {
    return Completable.fromAction(() -> cache.saveSync(key, paymentTransaction))
        .andThen(getBalanceState(paymentTransaction.getTransactionBuilder()).observeOn(scheduler)
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
                  return action;
              }
            }))
        .onErrorResumeNext(throwable -> {
          PaymentError paymentError = paymentErrorMapper.map(throwable);
          return cache.save(paymentTransaction.getUri(),
              new PaymentTransaction(paymentTransaction, paymentError.getPaymentState(),
                  paymentError.getErrorCode(), paymentError.getErrorMessage()));
        });
  }

  private Single<PaymentTransaction> mapTransactionToPaymentTransaction(
      ApproveService.ApproveTransaction approveTransaction) {
    return cache.get(approveTransaction.getKey())
        .firstOrError()
        .map(paymentTransaction -> new PaymentTransaction(paymentTransaction,
            getStatus(approveTransaction.getStatus()), approveTransaction.getTransactionHash()));
  }

  private PaymentTransaction.PaymentState getStatus(ApproveService.Status status) {
    PaymentTransaction.PaymentState toReturn;
    switch (status) {
      case PENDING:
        toReturn = PaymentTransaction.PaymentState.PENDING;
        break;
      case APPROVING:
        toReturn = PaymentTransaction.PaymentState.APPROVING;
        break;
      case APPROVED:
        toReturn = PaymentTransaction.PaymentState.APPROVED;
        break;
      default:
      case ERROR:
        toReturn = PaymentTransaction.PaymentState.ERROR;
        break;
      case WRONG_NETWORK:
        toReturn = PaymentTransaction.PaymentState.WRONG_NETWORK;
        break;
      case NONCE_ERROR:
        toReturn = PaymentTransaction.PaymentState.NONCE_ERROR;
        break;
      case UNKNOWN_TOKEN:
        toReturn = PaymentTransaction.PaymentState.UNKNOWN_TOKEN;
        break;
      case NO_TOKENS:
        toReturn = PaymentTransaction.PaymentState.NO_TOKENS;
        break;
      case NO_ETHER:
        toReturn = PaymentTransaction.PaymentState.NO_ETHER;
        break;
      case NO_FUNDS:
        toReturn = PaymentTransaction.PaymentState.NO_FUNDS;
        break;
      case NO_INTERNET:
        toReturn = PaymentTransaction.PaymentState.NO_INTERNET;
        break;
      case FORBIDDEN:
        toReturn = PaymentTransaction.PaymentState.FORBIDDEN;
        break;
      case SUB_ALREADY_OWNED:
        toReturn = PaymentTransaction.PaymentState.SUB_ALREADY_OWNED;
        break;
    }
    return toReturn;
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
      case FORBIDDEN:
        paymentState = PaymentTransaction.PaymentState.FORBIDDEN;
        break;
      case SUB_ALREADY_OWNED:
        paymentState = PaymentTransaction.PaymentState.SUB_ALREADY_OWNED;
        break;
    }
    return paymentState;
  }

  public void start() {
    approveService.start();
    buyService.start();
    approveService.getAll()
        .flatMapCompletable(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .flatMapCompletable(approveTransaction -> mapTransactionToPaymentTransaction(
                approveTransaction).flatMap(
                paymentTransaction -> cache.save(paymentTransaction.getUri(), paymentTransaction)
                    .toSingleDefault(paymentTransaction))
                .filter(transaction -> transaction.getState()
                    .equals(PaymentTransaction.PaymentState.APPROVED))
                .flatMapCompletable(transaction -> {
                  String uri = transaction.getUri();
                  return transaction.getTransactionBuilder()
                      .amount()
                      .equals(BigDecimal.ZERO) ? approveService.remove(uri)
                      : approveService.remove(uri)
                          .andThen(buyService.buy(uri, transaction)
                              .onErrorResumeNext(throwable -> {
                                PaymentError paymentError = paymentErrorMapper.map(throwable);
                                return cache.save(uri, new PaymentTransaction(transaction,
                                    paymentError.getPaymentState(), paymentError.getErrorCode(),
                                    paymentError.getErrorMessage()));
                              }));
                })))
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
                .flatMapCompletable(transaction -> buyService.remove(transaction.getUri())
                    .andThen(cache.save(transaction.getUri(), new PaymentTransaction(transaction,
                        PaymentTransaction.PaymentState.COMPLETED))))))
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

  public Single<Boolean> hasBalanceToBuy(TransactionBuilder transactionBuilder) {
    return getBalanceState(transactionBuilder).flatMap(balanceState -> {
      if (balanceState.equals(OK)) {
        return Single.just(true);
      } else {
        return Single.just(false);
      }
    });
  }

  public Single<BalanceState> getBalanceState(TransactionBuilder transactionBuilder) {
    BigDecimal transactionGasLimit = transactionBuilder.gasSettings().gasLimit;
    GasSettings gasSettings = transactionBuilder.gasSettings();
    if (transactionBuilder.shouldSendToken()) {
      return checkTokenAddress(transactionBuilder, true).flatMap(
          __ -> Single.zip(hasEnoughForTransfer(transactionBuilder),
              hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit)),
              this::mapToState));
    } else {
      return Single.zip(hasEnoughForTransfer(transactionBuilder),
          hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit)), this::mapToState);
    }
  }

  private Single<Boolean> hasEnoughForTransfer(TransactionBuilder transactionBuilder) {
    if (transactionBuilder.shouldSendToken()) {
      return hasEnoughBalanceUseCase.invoke(null, transactionBuilder.amount(), Convert.Unit.ETHER,
          HasEnoughBalanceUseCase.BalanceType.APPC);
    } else {
      return hasEnoughBalanceUseCase.invoke(null, transactionBuilder.amount(), Convert.Unit.WEI,
          HasEnoughBalanceUseCase.BalanceType.ETH);
    }
  }

  private Single<Boolean> hasEnoughForFee(BigDecimal fee) {
    return hasEnoughBalanceUseCase.invoke(null, fee, Convert.Unit.WEI,
        HasEnoughBalanceUseCase.BalanceType.ETH);
  }

  private <T> Single<T> checkTokenAddress(TransactionBuilder transactionBuilder, T successValue) {
    return defaultTokenProvider.getDefaultToken()
        .flatMap(tokenInfo -> {
          if (tokenInfo.address.equalsIgnoreCase(transactionBuilder.contractAddress())) {
            return Single.just(successValue);
          } else {
            return Single.error(new UnknownTokenException());
          }
        });
  }

  @NonNull private BalanceState mapToState(Boolean enoughEther, boolean enoughTokens) {
    if (enoughTokens && enoughEther) {
      return OK;
    } else if (!enoughTokens && !enoughEther) {
      return BalanceState.NO_ETHER_NO_TOKEN;
    } else if (enoughEther) {
      return BalanceState.NO_TOKEN;
    } else {
      return BalanceState.NO_ETHER;
    }
  }

  public enum BalanceState {
    NO_TOKEN, NO_ETHER, NO_ETHER_NO_TOKEN, OK
  }
}
