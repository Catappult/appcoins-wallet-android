package com.asfoundation.wallet.ui.iab;

import androidx.annotation.NonNull;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.asfoundation.wallet.base.RxSchedulers;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.repository.BdsTransactionService;
import com.asfoundation.wallet.repository.CurrencyConversionService;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.repository.TransactionNotFoundException;
import com.asfoundation.wallet.util.TransferParser;
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;

public class AsfInAppPurchaseInteractor {
  private final InAppPurchaseService inAppPurchaseService;
  private final CurrencyConversionService currencyConversionService;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final BigDecimal paymentGasLimit;
  private final TransferParser parser;
  private final BillingMessagesMapper billingMessagesMapper;
  private final Billing billing;
  private final BdsTransactionService trackTransactionService;
  private final RxSchedulers rxSchedulers;

  public AsfInAppPurchaseInteractor(InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      @Named("payment-gas-limit") BigDecimal paymentGasLimit, TransferParser parser,
      BillingMessagesMapper billingMessagesMapper, Billing billing,
      CurrencyConversionService currencyConversionService,
      BdsTransactionService trackTransactionService, RxSchedulers rxSchedulers) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsInteract = gasSettingsInteract;
    this.paymentGasLimit = paymentGasLimit;
    this.parser = parser;
    this.billingMessagesMapper = billingMessagesMapper;
    this.billing = billing;
    this.currencyConversionService = currencyConversionService;
    this.trackTransactionService = trackTransactionService;
    this.rxSchedulers = rxSchedulers;
  }

  Single<TransactionBuilder> parseTransaction(String uri) {
    return parser.parse(uri);
  }

  public Completable send(String uri, TransactionType transactionType, String packageName,
      String productName, String developerPayload, TransactionBuilder transactionBuilder) {
    if (transactionType == TransactionType.NORMAL) {
      return buildPaymentTransaction(uri, packageName, productName, developerPayload,
          transactionBuilder.amount()).flatMapCompletable(
          paymentTransaction -> inAppPurchaseService.send(paymentTransaction.getUri(),
              paymentTransaction));
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction type " + transactionType + " not supported"));
  }

  Completable resume(String uri, TransactionType transactionType, String packageName,
      String productName, String approveKey, String developerPayload,
      TransactionBuilder transactionBuilder) {
    if (transactionType == TransactionType.NORMAL) {
      return buildPaymentTransaction(uri, packageName, productName, developerPayload,
          transactionBuilder.amount()).flatMapCompletable(
          paymentTransaction -> getSkuTransaction(paymentTransaction, packageName, approveKey));
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction type " + transactionType + " not supported"));
  }

  private Completable getSkuTransaction(PaymentTransaction paymentTransaction, String packageName,
      String approveKey) {
    BillingSupportedType billingType = BillingSupportedType.valueOfInsensitive(
        paymentTransaction.getTransactionBuilder()
            .getType());
    return billing.getSkuTransaction(packageName, paymentTransaction.getTransactionBuilder()
        .getSkuId(), rxSchedulers.getIo(), billingType)
        .flatMapCompletable(
            transaction -> resumePayment(approveKey, paymentTransaction, transaction));
  }

  private Completable resumePayment(String approveKey, PaymentTransaction paymentTransaction,
      Transaction transaction) {
    switch (transaction.getStatus()) {
      case PENDING_SERVICE_AUTHORIZATION:
        return inAppPurchaseService.resume(paymentTransaction.getUri(),
            new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVED,
                approveKey));
      case PROCESSING:
        return trackTransactionService.trackTransaction(paymentTransaction.getUri(),
            paymentTransaction.getPackageName(), paymentTransaction.getTransactionBuilder()
                .getSkuId(), transaction.getUid(), null, transaction.getOrderReference());
      case PENDING:
      case COMPLETED:
      case INVALID_TRANSACTION:
      case FAILED:
      case CANCELED:
      default:
        return Completable.error(new UnsupportedOperationException(
            "Cannot resume from " + transaction.getStatus() + " state"));
    }
  }

  Observable<Payment> getTransactionState(String uri) {
    return Observable.merge(inAppPurchaseService.getTransactionState(uri)
        .map(this::mapToPayment), trackTransactionService.getTransaction(uri)
        .map(this::map));
  }

  private Payment map(BdsTransactionService.BdsTransaction transaction) {
    return new Payment(transaction.getKey(), mapStatus(transaction.getStatus()), null, null,
        transaction.getPackageName(), null, transaction.getSkuId(), transaction.getOrderReference(),
        null, null);
  }

  private Payment.Status mapStatus(BdsTransactionService.BdsTransaction.Status status) {
    switch (status) {
      default:
      case WAITING:
      case UNKNOWN_STATUS:
        return Payment.Status.ERROR;
      case PROCESSING:
        return Payment.Status.BUYING;
      case COMPLETED:
        return Payment.Status.COMPLETED;
    }
  }

  @NonNull private Payment mapToPayment(PaymentTransaction paymentTransaction) {
    return new Payment(paymentTransaction.getUri(), mapStatus(paymentTransaction.getState()),
        paymentTransaction.getTransactionBuilder()
            .fromAddress(), paymentTransaction.getBuyHash(), paymentTransaction.getPackageName(),
        paymentTransaction.getProductName(), paymentTransaction.getProductId(),
        paymentTransaction.getOrderReference(), paymentTransaction.getErrorCode(),
        paymentTransaction.getErrorMessage());
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
      case FORBIDDEN:
        return Payment.Status.FORBIDDEN;
      case SUB_ALREADY_OWNED:
        return Payment.Status.SUB_ALREADY_OWNED;
    }
    throw new IllegalStateException("State " + state + " not mapped");
  }

  public Completable remove(String uri) {
    return inAppPurchaseService.remove(uri)
        .andThen(trackTransactionService.remove(uri));
  }

  private Single<PaymentTransaction> buildPaymentTransaction(String uri, String packageName,
      String productName, String developerPayload, BigDecimal amount) {
    return Single.zip(parseTransaction(uri).observeOn(rxSchedulers.getIo()),
            defaultWalletInteract.find()
                .observeOn(rxSchedulers.getIo()),
            (transaction, wallet) -> transaction.fromAddress(wallet.getAddress()))
        .flatMap(transactionBuilder -> gasSettingsInteract.fetch(true)
            .map(gasSettings -> {
              transactionBuilder.gasSettings(new GasSettings(
                  gasSettings.gasPrice,
                  paymentGasLimit));
              return transactionBuilder.amount(amount);
            }))
        .map(transactionBuilder -> new PaymentTransaction(uri, transactionBuilder, packageName,
            productName, transactionBuilder.getSkuId(), developerPayload,
            transactionBuilder.getCallbackUrl(), transactionBuilder.getOrderReference()));
  }

  public void start() {
    inAppPurchaseService.start();
    trackTransactionService.start();
  }

  public Observable<List<Payment>> getAll() {
    return inAppPurchaseService.getAll()
        .flatMapSingle(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .map(paymentTransaction -> new Payment(paymentTransaction.getUri(),
                mapStatus(paymentTransaction.getState()), paymentTransaction.getTransactionBuilder()
                .fromAddress(), paymentTransaction.getBuyHash(),
                paymentTransaction.getPackageName(), paymentTransaction.getProductName(),
                paymentTransaction.getProductId(), null, paymentTransaction.getErrorCode(),
                paymentTransaction.getErrorMessage()))
            .toList());
  }

  List<BigDecimal> getTopUpChannelSuggestionValues(BigDecimal price) {
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

  public Single<String> getWalletAddress() {
    return defaultWalletInteract.find()
        .map(Wallet::getAddress);
  }

  Single<CurrentPaymentStep> getCurrentPaymentStep(String packageName,
      TransactionBuilder transactionBuilder) {
    return Single.zip(
        getTransaction(packageName, transactionBuilder.getSkuId(), transactionBuilder.getType()),
        isAppcoinsPaymentReady(transactionBuilder), this::map);
  }

  Single<Boolean> isAppcoinsPaymentReady(TransactionBuilder transactionBuilder) {
    return gasSettingsInteract.fetch(true)
        .doOnSuccess(gasSettings -> transactionBuilder.gasSettings(
            new GasSettings(gasSettings.gasPrice,
                paymentGasLimit)))
        .flatMap(__ -> inAppPurchaseService.hasBalanceToBuy(transactionBuilder));
  }

  Single<InAppPurchaseService.BalanceState> getAppcoinsBalanceState(
      TransactionBuilder transactionBuilder) {
    return gasSettingsInteract.fetch(true)
        .doOnSuccess(gasSettings -> transactionBuilder.gasSettings(
            new GasSettings(gasSettings.gasPrice,
                paymentGasLimit)))
        .flatMap(__ -> inAppPurchaseService.getBalanceState(transactionBuilder));
  }

  private CurrentPaymentStep map(Transaction transaction, Boolean isBuyReady)
      throws UnknownServiceException {
    switch (transaction.getStatus()) {
      case PENDING:
      case PENDING_SERVICE_AUTHORIZATION:
      case PROCESSING:
        switch (transaction.getGateway()
            .getName()) {
          case appcoins:
            return CurrentPaymentStep.PAUSED_ON_CHAIN;
          case adyen_v2:
            if (transaction.getStatus()
                .equals(Transaction.Status.PROCESSING)) {
              return CurrentPaymentStep.PAUSED_CC_PAYMENT;
            } else {
              return isBuyReady ? CurrentPaymentStep.READY : CurrentPaymentStep.NO_FUNDS;
            }
          case myappcoins:
            return CurrentPaymentStep.PAUSED_LOCAL_PAYMENT;
          case appcoins_credits:
            return CurrentPaymentStep.PAUSED_CREDITS;
          default:
          case unknown:
            throw new UnknownServiceException("Unknown gateway");
        }
      case COMPLETED:
      case PENDING_USER_PAYMENT:
      case FAILED:
      case CANCELED:
      case INVALID_TRANSACTION:
      default:
        return isBuyReady ? CurrentPaymentStep.READY : CurrentPaymentStep.NO_FUNDS;
    }
  }

  Single<FiatValue> convertToFiat(double appcValue, String currency) {
    return currencyConversionService.getTokenValue(currency)
        .map(fiatValueConversion -> calculateValue(fiatValueConversion, appcValue));
  }

  Single<FiatValue> convertToLocalFiat(double appcValue) {
    return currencyConversionService.getLocalFiatAmount(Double.toString(appcValue));
  }

  Single<FiatValue> convertFiatToLocalFiat(double value, String currency) {
    return currencyConversionService.getLocalFiatAmount(Double.toString(value), currency);
  }

  Single<FiatValue> convertFiatToAppc(double value, String currency) {
    return currencyConversionService.getFiatToAppcAmount(Double.toString(value), currency);
  }

  private FiatValue calculateValue(FiatValue fiatValue, double appcValue) {
    return new FiatValue(fiatValue.getAmount()
        .multiply(BigDecimal.valueOf(appcValue)), fiatValue.getCurrency(), fiatValue.getSymbol());
  }

  public BillingMessagesMapper getBillingMessagesMapper() {
    return billingMessagesMapper;
  }

  private Single<Transaction> getTransaction(String packageName, String productName, String type) {
    return Single.defer(() -> {
      BillingSupportedType billingType = BillingSupportedType.valueOfInsensitive(type);
      return billing.getSkuTransaction(packageName, productName, Schedulers.io(), billingType);
    });
  }

  Single<Purchase> getCompletedPurchase(String packageName, String productName, String purchaseUid,
      String type) {
    BillingSupportedType billingType = BillingSupportedType.valueOfManagedType(type);
    return billing.getSkuTransaction(packageName, productName, Schedulers.io(), billingType)
        .flatMap(transaction -> {
          if (transaction.getStatus()
              .equals(Transaction.Status.COMPLETED)) {
            return billing.getSkuPurchase(packageName, productName, purchaseUid, Schedulers.io(),
                billingType);
          } else {
            return Single.error(new TransactionNotFoundException());
          }
        });
  }

  public enum TransactionType {
    NORMAL
  }

  public enum CurrentPaymentStep {
    PAUSED_CC_PAYMENT, PAUSED_ON_CHAIN, NO_FUNDS, PAUSED_LOCAL_PAYMENT, PAUSED_CREDITS, READY
  }
}
