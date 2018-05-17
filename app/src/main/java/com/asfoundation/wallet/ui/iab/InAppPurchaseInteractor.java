package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperation;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.List;

public class InAppPurchaseInteractor {
  public static final double GAS_PRICE_MULTIPLIER = 1.25;
  private final InAppPurchaseService inAppPurchaseService;
  private final AppcoinsOperationsDataSaver inAppPurchaseDataSaver;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final BigDecimal paymentGasLimit;
  private final TransferParser parser;

  public InAppPurchaseInteractor(InAppPurchaseService inAppPurchaseService,
      AppcoinsOperationsDataSaver inAppPurchaseDataSaver,
      FindDefaultWalletInteract defaultWalletInteract, FetchGasSettingsInteract gasSettingsInteract,
      BigDecimal paymentGasLimit, TransferParser parser) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.inAppPurchaseDataSaver = inAppPurchaseDataSaver;
    this.defaultWalletInteract = defaultWalletInteract;
    this.gasSettingsInteract = gasSettingsInteract;
    this.paymentGasLimit = paymentGasLimit;
    this.parser = parser;
  }

  public Observable<AppCoinsOperation> getPurchaseData(String id) {
    return inAppPurchaseDataSaver.get(id);
  }

  public Observable<List<AppCoinsOperation>> getAllPurchasesData() {
    return inAppPurchaseDataSaver.getAll();
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
    inAppPurchaseDataSaver.start();
    inAppPurchaseService.start();
  }

  public void stop() {
    inAppPurchaseDataSaver.stop();
  }
}
