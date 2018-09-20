package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.billing.repository.entity.Purchase;
import com.appcoins.wallet.billing.repository.entity.Transaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.net.UnknownServiceException;
import java.util.List;

public class InAppPurchaseInteractor {
  public static final double GAS_PRICE_MULTIPLIER = 1.25;
  private static final String TAG = InAppPurchaseInteractor.class.getSimpleName();

  private final AsfInAppPurchaseInteractor asfInAppPurchaseInteractor;
  private final BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor;

  public InAppPurchaseInteractor(AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
  }

  public Single<TransactionBuilder> parseTransaction(String uri, boolean isBds) {
    if (isBds) {
      return asfInAppPurchaseInteractor.parseTransaction(uri);
    } else {
      return bdsInAppPurchaseInteractor.parseTransaction(uri);
    }
  }

  public Completable send(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, BigDecimal channelBudget, String developerPayload,
      boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
          channelBudget, developerPayload);
    } else {
      return asfInAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
          channelBudget, developerPayload);
    }
  }

  public Completable resume(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload, boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
          developerPayload);
    } else {
      return Completable.error(new UnsupportedOperationException("Asf doesn't support resume."));
    }
  }

  public Observable<Payment> getTransactionState(String uri) {
    return Observable.merge(asfInAppPurchaseInteractor.getTransactionState(uri),
        bdsInAppPurchaseInteractor.getTransactionState(uri));
  }

  public Completable remove(String uri) {
    return asfInAppPurchaseInteractor.remove(uri)
        .andThen(bdsInAppPurchaseInteractor.remove(uri));
  }

  public void start() {
    asfInAppPurchaseInteractor.start();
    bdsInAppPurchaseInteractor.start();
  }

  public Observable<List<Payment>> getAll() {
    return Observable.merge(asfInAppPurchaseInteractor.getAll(),
        bdsInAppPurchaseInteractor.getAll());
  }

  public List<BigDecimal> getTopUpChannelSuggestionValues(BigDecimal price) {
    return bdsInAppPurchaseInteractor.getTopUpChannelSuggestionValues(price);
  }

  public boolean shouldShowDialog() {
    return bdsInAppPurchaseInteractor.shouldShowDialog();
  }

  public void dontShowAgain() {
    bdsInAppPurchaseInteractor.dontShowAgain();
  }

  public Single<Boolean> hasChannel() {
    return bdsInAppPurchaseInteractor.hasChannel();
  }

  public Single<String> getWalletAddress() {
    return asfInAppPurchaseInteractor.getWalletAddress();
  }

  public Single<AsfInAppPurchaseInteractor.CurrentPaymentStep> getCurrentPaymentStep(
      String packageName,
      TransactionBuilder transactionBuilder) {
    return asfInAppPurchaseInteractor.getCurrentPaymentStep(packageName, transactionBuilder);
  }

  private AsfInAppPurchaseInteractor.CurrentPaymentStep map(Transaction transaction,
      Boolean isBuyReady)
      throws UnknownServiceException {
    switch (transaction.getStatus()) {
      case PENDING:
      case PENDING_SERVICE_AUTHORIZATION:
      case PROCESSING:
        switch (transaction.getGateway()
            .getName()) {
          case appcoins:
            return AsfInAppPurchaseInteractor.CurrentPaymentStep.PAUSED_ON_CHAIN;
          case adyen:
            return AsfInAppPurchaseInteractor.CurrentPaymentStep.PAUSED_OFF_CHAIN;
          default:
          case unknown:
            throw new UnknownServiceException("Unknown gateway");
        }
      case COMPLETED:
        return isBuyReady ? AsfInAppPurchaseInteractor.CurrentPaymentStep.READY
            : AsfInAppPurchaseInteractor.CurrentPaymentStep.NO_FUNDS;
      default:
      case FAILED:
      case CANCELED:
      case INVALID_TRANSACTION:
        return isBuyReady ? AsfInAppPurchaseInteractor.CurrentPaymentStep.READY
            : AsfInAppPurchaseInteractor.CurrentPaymentStep.NO_FUNDS;
    }
  }

  public Single<FiatValue> convertToFiat(double appcValue, String currency) {
    return asfInAppPurchaseInteractor.convertToFiat(appcValue, currency);
  }

  private FiatValue calculateValue(FiatValue fiatValue, double appcValue) {
    return new FiatValue(fiatValue.getAmount() * appcValue, fiatValue.getCurrency());
  }

  public BillingMessagesMapper getBillingMessagesMapper() {
    return bdsInAppPurchaseInteractor.getBillingMessagesMapper();
  }

  public ExternalBillingSerializer getBillingSerializer() {
    return bdsInAppPurchaseInteractor.getBillingSerializer();
  }

  public Single<Transaction> getTransaction(String packageName, String productName) {
    return asfInAppPurchaseInteractor.getTransaction(packageName, productName);
  }

  public Single<Purchase> getCompletedPurchase(String packageName, String productName) {
    return bdsInAppPurchaseInteractor.getCompletedPurchase(packageName, productName);
  }
}
