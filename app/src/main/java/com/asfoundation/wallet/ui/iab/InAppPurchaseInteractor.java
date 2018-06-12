package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.ui.iab.raiden.ChannelNotFoundException;
import com.asfoundation.wallet.ui.iab.raiden.Raiden;
import com.asfoundation.wallet.ui.iab.raiden.RaidenRepository;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InAppPurchaseInteractor {
  public static final double GAS_PRICE_MULTIPLIER = 1.25;
  private final InAppPurchaseService inAppPurchaseService;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final BigDecimal paymentGasLimit;
  private final TransferParser parser;
  private final RaidenRepository raidenRepository;
  private final Raiden raiden;

  public InAppPurchaseInteractor(InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      BigDecimal paymentGasLimit, TransferParser parser, RaidenRepository raidenRepository,
      Raiden raiden) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsInteract = gasSettingsInteract;
    this.paymentGasLimit = paymentGasLimit;
    this.parser = parser;
    this.raidenRepository = raidenRepository;
    this.raiden = raiden;
  }

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return parser.parse(uri);
  }

  public Completable send(String uri, TransactionType transactionType, String packageName,
      String productName) {
    switch (transactionType) {
      case NORMAL:
        return buildPaymentTransaction(uri, packageName, productName).flatMapCompletable(
            paymentTransaction -> inAppPurchaseService.send(paymentTransaction.getUri(),
                paymentTransaction));
      case RAIDEN:
        return buildPaymentTransaction(uri, packageName, productName).observeOn(Schedulers.io())
            .flatMapCompletable(paymentTransaction -> raiden.buy(paymentTransaction)
                .retryWhen(throwableFlowable -> throwableFlowable.flatMap(throwable -> {
                  if (throwable instanceof ChannelNotFoundException) {
                    return raiden.createChannel(paymentTransaction.getTransactionBuilder()
                        .fromAddress(), BigDecimal.ONE)
                        .andThen(Observable.just(new Object()))
                        .toFlowable(BackpressureStrategy.DROP);
                  } else {
                    return Flowable.error(throwable);
                  }
                })));
    }
    return Completable.error(
        new IllegalArgumentException("Transaction type " + transactionType + " not supported"));
  }

  public Observable<Payment> getTransactionState(String uri) {
    return inAppPurchaseService.getTransactionState(uri)
        .map(paymentTransaction -> new Payment(paymentTransaction.getUri(),
            mapStatus(paymentTransaction.getState()), paymentTransaction.getBuyHash()));
  }

  private Payment.Status mapStatus(PaymentTransaction.PaymentState state) {
    switch (state) {
      case PENDING:
      case APPROVING:
      case APPROVED:
        return Payment.Status.APPROVING;
      case BUYING:
      case BOUGHT:
        return Payment.Status.BUYING;
      case COMPLETED:
        return Payment.Status.COMPLETED;
      case ERROR:
        return Payment.Status.ERROR;
      case WRONG_NETWORK:
      case UNKNOWN_TOKEN:
        return Payment.Status.NETWORK_ERROR;
      case NONCE_ERROR:
        return Payment.Status.NONCE_ERROR;
      case NO_TOKENS:
        return Payment.Status.NO_TOKENS;
      case NO_ETHER:
        return Payment.Status.NO_ETHER;
      case NO_FUNDS:
        return Payment.Status.NO_FUNDS;
      case NO_INTERNET:
        return Payment.Status.NO_INTERNET;
    }
    throw new IllegalStateException("State " + state + " not mapped");
  }

  public Completable remove(String uri) {
    return inAppPurchaseService.remove(uri);
  }

  private Single<PaymentTransaction> buildPaymentTransaction(String uri, String packageName,
      String productName) {
    return Single.zip(parseTransaction(uri), defaultWalletInteract.find(),
        (transaction, wallet) -> transaction.fromAddress(wallet.address))
        .flatMap(transactionBuilder -> gasSettingsInteract.fetch(true)
            .map(gasSettings -> transactionBuilder.gasSettings(
                new GasSettings(gasSettings.gasPrice.multiply(new BigDecimal(GAS_PRICE_MULTIPLIER)),
                    paymentGasLimit))))
        .map(transactionBuilder -> new PaymentTransaction(uri, transactionBuilder, packageName,
            productName));
  }

  public void start() {
    inAppPurchaseService.start();
  }

  public Observable<List<PaymentTransaction>> getAll() {
    return inAppPurchaseService.getAll();
  }

  public List<BigDecimal> getTopUpChannelSuggestionValues(BigDecimal price) {
    BigDecimal firstValue =
        price.add(new BigDecimal(5).subtract((price.remainder(new BigDecimal(5)))));
    ArrayList<BigDecimal> list = new ArrayList<>();
    list.add(price);
    list.add(firstValue);
    list.add(firstValue.add(new BigDecimal(5)));
    list.add(firstValue.add(new BigDecimal(15)));
    list.add(firstValue.add(new BigDecimal(25)));
    return list;
  }

  public boolean shouldShowDialog() {
    return raidenRepository.shouldShowDialog();
  }

  public void dontShowAgain() {
    raidenRepository.setShouldShowDialog(false);
  }

  public enum TransactionType {
    NORMAL, RAIDEN
  }
}
