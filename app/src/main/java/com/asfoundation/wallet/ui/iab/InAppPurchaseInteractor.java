package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.math.BigDecimal;
import java.util.List;

public class InAppPurchaseInteractor {
  public static final double GAS_PRICE_MULTIPLIER = 1.25;
  private static final String TAG = InAppPurchaseInteractor.class.getSimpleName();

  private final AsfInAppPurchaseInteractor asfInAppPurchaseInteractor;
  private final BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor;
  private final BillingMessagesMapper billingMessagesMapper;
  private final ExternalBillingSerializer billingSerializer;

  public InAppPurchaseInteractor(AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      BillingMessagesMapper billingMessagesMapper, ExternalBillingSerializer billingSerializer) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
    this.billingMessagesMapper = billingMessagesMapper;
    this.billingSerializer = billingSerializer;
  }

  public Single<TransactionBuilder> parseTransaction(String uri, boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.parseTransaction(uri);
    } else {
      return asfInAppPurchaseInteractor.parseTransaction(uri);
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
      String packageName, TransactionBuilder transactionBuilder) {
    return asfInAppPurchaseInteractor.getCurrentPaymentStep(packageName, transactionBuilder);
  }

  public Single<FiatValue> convertToFiat(double appcValue, String currency) {
    return asfInAppPurchaseInteractor.convertToFiat(appcValue, currency);
  }

  public BillingMessagesMapper getBillingMessagesMapper() {
    return bdsInAppPurchaseInteractor.getBillingMessagesMapper();
  }

  public ExternalBillingSerializer getBillingSerializer() {
    return bdsInAppPurchaseInteractor.getBillingSerializer();
  }

  public Single<Transaction> getTransaction(String packageName, String productName, String type) {
    return asfInAppPurchaseInteractor.getTransaction(packageName, productName, type);
  }

  private Single<Purchase> getCompletedPurchase(String packageName, String productName) {
    return bdsInAppPurchaseInteractor.getCompletedPurchase(packageName, productName);
  }

  public Single<Payment> getCompletedPurchase(Payment transaction, boolean isBds) {
    if (isBds) {
      return getCompletedPurchase(transaction.getPackageName(), transaction.getProductId()).map(
          purchase -> mapToBdsPayment(transaction, purchase))
          .observeOn(AndroidSchedulers.mainThread())
          .flatMap(payment -> remove(transaction.getUri()).toSingleDefault(payment));
    } else {
      return Single.fromCallable(() -> transaction)
          .flatMap(bundle -> remove(transaction.getUri()).toSingleDefault(bundle));
    }
  }

  private Payment mapToBdsPayment(Payment transaction, Purchase purchase) {
    return new Payment(transaction.getUri(), transaction.getStatus(), purchase.getUid(),
        purchase.getSignature()
            .getValue(), billingSerializer.serializeSignatureData(purchase));
  }

  public Single<Boolean> isWalletFromBds(String packageName, String wallet) {
    return bdsInAppPurchaseInteractor.isBdsWallet(packageName)
        .map(wallet::equalsIgnoreCase);
  }
}
