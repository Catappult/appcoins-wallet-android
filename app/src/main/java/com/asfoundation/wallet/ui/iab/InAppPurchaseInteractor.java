package com.asfoundation.wallet.ui.iab;

import android.support.annotation.NonNull;
import com.appcoins.wallet.billing.BillingFactory;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.billing.repository.entity.Purchase;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.ExpressCheckoutBuyService;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.repository.TransactionNotFoundException;
import com.asfoundation.wallet.ui.iab.raiden.ChannelCreation;
import com.asfoundation.wallet.ui.iab.raiden.ChannelPayment;
import com.asfoundation.wallet.ui.iab.raiden.ChannelService;
import com.asfoundation.wallet.ui.iab.raiden.RaidenRepository;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InAppPurchaseInteractor {
  public static final double GAS_PRICE_MULTIPLIER = 1.25;
  private static final String TAG = InAppPurchaseInteractor.class.getSimpleName();
  private final InAppPurchaseService inAppPurchaseService;
  private final ExpressCheckoutBuyService expressCheckoutBuyService;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final BigDecimal paymentGasLimit;
  private final TransferParser parser;
  private final RaidenRepository raidenRepository;
  private final ChannelService channelService;
  private final BillingMessagesMapper billingMessagesMapper;
  private final BillingFactory billingFactory;
  private final ExternalBillingSerializer billingSerializer;

  public InAppPurchaseInteractor(InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      BigDecimal paymentGasLimit, TransferParser parser, RaidenRepository raidenRepository,
      ChannelService channelService, BillingMessagesMapper billingMessagesMapper,
      BillingFactory billingFactory, ExternalBillingSerializer billingSerializer,
      ExpressCheckoutBuyService expressCheckoutBuyService) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsInteract = gasSettingsInteract;
    this.paymentGasLimit = paymentGasLimit;
    this.parser = parser;
    this.raidenRepository = raidenRepository;
    this.channelService = channelService;
    this.billingMessagesMapper = billingMessagesMapper;
    this.billingFactory = billingFactory;
    this.billingSerializer = billingSerializer;
    this.expressCheckoutBuyService = expressCheckoutBuyService;
  }

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return parser.parse(uri);
  }

  public Completable send(String uri, TransactionType transactionType, String packageName,
      String productName, BigDecimal channelBudget) {
    switch (transactionType) {
      case NORMAL:
        return buildPaymentTransaction(uri, packageName, productName).flatMapCompletable(
            paymentTransaction -> inAppPurchaseService.send(paymentTransaction.getUri(),
                paymentTransaction));
      case RAIDEN:
        return buildPaymentTransaction(uri, packageName, productName).observeOn(Schedulers.io())
            .flatMapCompletable(paymentTransaction -> channelService.hasChannel(
                paymentTransaction.getTransactionBuilder()
                    .fromAddress())
                .flatMapCompletable(hasChannel -> {
                  if (hasChannel) {
                    return makePayment(paymentTransaction);
                  }
                  return channelService.createChannelAndBuy(paymentTransaction.getUri(),
                      paymentTransaction.getTransactionBuilder()
                          .fromAddress(), channelBudget, paymentTransaction);
                }));
    }
    return Completable.error(
        new IllegalArgumentException("Transaction type " + transactionType + " not supported"));
  }

  private Completable makePayment(PaymentTransaction paymentTransaction) {
    return channelService.hasFunds(paymentTransaction.getTransactionBuilder()
        .fromAddress(), paymentTransaction.getTransactionBuilder()
        .amount())
        .flatMapCompletable(hasFunds -> {
          if (hasFunds) {
            return Completable.fromAction(() -> channelService.buy(paymentTransaction));
          }
          return Completable.error(new NotEnoughFundsException());
        });
  }

  public Observable<Payment> getTransactionState(String uri) {
    return Observable.merge(inAppPurchaseService.getTransactionState(uri)
        .map(this::mapToPayment), channelService.getPayment(uri)
        .map(this::mapToPayment), channelService.getChannel(uri)
        .map(this::mapToPayment));
  }

  private Payment mapToPayment(ChannelCreation creation) {
    switch (creation.getStatus()) {
      case PENDING:
        return new Payment(creation.getKey(), Payment.Status.APPROVING);
      case CREATING:
        return new Payment(creation.getKey(), Payment.Status.APPROVING);
      case CREATED:
        return new Payment(creation.getKey(), Payment.Status.APPROVING);
      case ERROR:
        return new Payment(creation.getKey(), Payment.Status.ERROR);
    }
    throw new IllegalStateException("Status " + creation.getStatus() + " not mapped");
  }

  @NonNull private Payment mapToPayment(PaymentTransaction paymentTransaction) {
    return new Payment(paymentTransaction.getUri(), mapStatus(paymentTransaction.getState()),
        paymentTransaction.getTransactionBuilder()
            .fromAddress(), paymentTransaction.getBuyHash(), paymentTransaction.getPackageName(),
        paymentTransaction.getProductName());
  }

  @NonNull private Payment mapToPayment(ChannelPayment channelPayment) {
    return new Payment(channelPayment.getId(), mapStatus(channelPayment.getStatus()),
        channelPayment.getFromAddress(), channelPayment.getHash(), channelPayment.getPackageName(),
        channelPayment.getProductName());
  }

  private Payment.Status mapStatus(ChannelPayment.Status status) {
    switch (status) {
      case PENDING:
      case BUYING:
        return Payment.Status.BUYING;
      case COMPLETED:
        return Payment.Status.COMPLETED;
      case ERROR:
        return Payment.Status.ERROR;
    }
    throw new IllegalStateException("Status " + status + " not mapped");
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
    return inAppPurchaseService.remove(uri)
        .andThen(channelService.remove(uri));
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
    channelService.start();
  }

  public Observable<List<Payment>> getAll() {
    return Observable.merge(channelService.getAll()
        .flatMapSingle(channelPayments -> Observable.fromIterable(channelPayments)
            .map(channelPayment -> new Payment(channelPayment.getId(),
                mapStatus(channelPayment.getStatus()), channelPayment.getFromAddress(),
                channelPayment.getHash(), channelPayment.getPackageName(),
                channelPayment.getProductName()))
            .toList()), inAppPurchaseService.getAll()
        .flatMapSingle(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .map(paymentTransaction -> new Payment(paymentTransaction.getUri(),
                mapStatus(paymentTransaction.getState()), paymentTransaction.getTransactionBuilder()
                .fromAddress(), paymentTransaction.getBuyHash(),
                paymentTransaction.getPackageName(), paymentTransaction.getProductName()))
            .toList()));
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

  public Single<Boolean> hasChannel() {
    return defaultWalletInteract.find()
        .observeOn(Schedulers.io())
        .flatMap(wallet -> channelService.hasChannel(wallet.address));
  }

  public Single<String> getWalletAddress() {
    return defaultWalletInteract.find()
        .map(wallet -> wallet.address);
  }

  public Single<Boolean> canBuy(TransactionBuilder transactionBuilder) {
    return gasSettingsInteract.fetch(true)
        .doOnSuccess(gasSettings -> transactionBuilder.gasSettings(
            new GasSettings(gasSettings.gasPrice.multiply(new BigDecimal(GAS_PRICE_MULTIPLIER)),
                paymentGasLimit)))
        .flatMap(__ -> inAppPurchaseService.hasBalanceToBuy(transactionBuilder));
  }

  public Single<FiatValue> convertToFiat(double appcValue, String currency) {
    return expressCheckoutBuyService.getTokenValue(currency)
        .map(fiatValueConvertion -> calculateValue(fiatValueConvertion, appcValue));
  }

  private FiatValue calculateValue(FiatValue fiatValue, double appcValue) {
    return new FiatValue(fiatValue.getAmount() * appcValue, fiatValue.getCurrency());
  }

  public BillingMessagesMapper getBillingMessagesMapper() {
    return billingMessagesMapper;
  }

  public ExternalBillingSerializer getBillingSerializer() {
    return billingSerializer;
  }

  public Single<Purchase> getPurchase(String packageName, String productName) {
    return Single.fromCallable(() -> billingFactory.getBilling(packageName))
        .flatMap(billing -> billing.getSkuTransactionStatus(productName, Schedulers.io())
            .flatMap(transactionStatus -> {
              if (transactionStatus.equalsIgnoreCase("COMPLETED")) {
                return billing.getSkuPurchase(productName, Schedulers.io());
              } else {
                return Single.error(new TransactionNotFoundException());
              }
            }));
  }

  public enum TransactionType {
    NORMAL, RAIDEN
  }
}
