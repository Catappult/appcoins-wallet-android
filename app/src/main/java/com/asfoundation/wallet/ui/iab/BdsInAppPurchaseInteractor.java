package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

public class BdsInAppPurchaseInteractor {
  private final AsfInAppPurchaseInteractor inAppPurchaseInteractor;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;
  private final ApproveKeyProvider approveKeyProvider;
  private final Billing billing;

  public @Inject BdsInAppPurchaseInteractor(
      @Named("ASF_BDS_IN_APP_INTERACTOR") AsfInAppPurchaseInteractor inAppPurchaseInteractor,
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      ApproveKeyProvider approveKeyProvider, Billing billing) {
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
    this.approveKeyProvider = approveKeyProvider;
    this.billing = billing;
  }

  public Single<TransactionBuilder> parseTransaction(String uri) {
    return inAppPurchaseInteractor.parseTransaction(uri);
  }

  public Completable send(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload,
      TransactionBuilder transactionBuilder) {
    return inAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
        developerPayload, transactionBuilder);
  }

  public Completable resume(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload, String type,
      TransactionBuilder transactionBuilder) {
    return approveKeyProvider.getTransaction(packageName, productName, type)
        .doOnSuccess(billingPaymentProofSubmission::saveTransactionId)
        .flatMapCompletable(
            transaction -> inAppPurchaseInteractor.resume(uri, transactionType, packageName,
                productName, transaction.getUid(), developerPayload, transactionBuilder));
  }

  public Observable<Payment> getTransactionState(String uri) {
    return inAppPurchaseInteractor.getTransactionState(uri);
  }

  public Completable remove(String uri) {
    return inAppPurchaseInteractor.remove(uri);
  }

  public void start() {
    inAppPurchaseInteractor.start();
  }

  public Observable<List<Payment>> getAll() {
    return inAppPurchaseInteractor.getAll();
  }

  public List<BigDecimal> getTopUpChannelSuggestionValues(BigDecimal price) {
    return inAppPurchaseInteractor.getTopUpChannelSuggestionValues(price);
  }

  public Single<String> getWalletAddress() {
    return inAppPurchaseInteractor.getWalletAddress();
  }

  public BillingMessagesMapper getBillingMessagesMapper() {
    return inAppPurchaseInteractor.getBillingMessagesMapper();
  }

  public Single<Purchase> getCompletedPurchase(String packageName, String productName,
      String purchaseUid, String type) {
    return inAppPurchaseInteractor.
        getCompletedPurchase(packageName, productName, purchaseUid, type);
  }

  public Single<String> getWallet(String packageName) {
    return billing.getWallet(packageName);
  }

  public Single<List<PaymentMethodEntity>> getPaymentMethods(String value, String currency,
      String transactionType, String packageName) {
    return billing.getPaymentMethods(value, currency, transactionType, packageName);
  }
}
