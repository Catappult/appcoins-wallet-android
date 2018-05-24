package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
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

  public InAppPurchaseInteractor(InAppPurchaseService inAppPurchaseService,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      BigDecimal paymentGasLimit, TransferParser parser) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsInteract = gasSettingsInteract;
    this.paymentGasLimit = paymentGasLimit;
    this.parser = parser;
  }

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return parser.parse(uri);
  }

  public Completable send(String uri, String packageName, String productName) {
    return buildPaymentTransaction(uri, packageName, productName).flatMapCompletable(
        paymentTransaction -> inAppPurchaseService.send(paymentTransaction.getUri(),
            paymentTransaction));
  }

  public Observable<PaymentTransaction> getTransactionState(String uri) {
    return inAppPurchaseService.getTransactionState(uri);
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
}
