package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.TransactionIdRepository;
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.BalanceUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.math.BigDecimal;
import java.util.List;

public class InAppPurchaseInteractor {

  private final AsfInAppPurchaseInteractor asfInAppPurchaseInteractor;
  private final BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor;
  private final ExternalBillingSerializer billingSerializer;
  private final AppcoinsRewards appcoinsRewards;
  private final TransactionIdRepository transactionIdRepository;

  public InAppPurchaseInteractor(AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      ExternalBillingSerializer billingSerializer, AppcoinsRewards appcoinsRewards,
      TransactionIdRepository transactionIdRepository) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
    this.billingSerializer = billingSerializer;
    this.appcoinsRewards = appcoinsRewards;
    this.transactionIdRepository = transactionIdRepository;
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
    return parseTransaction(transaction.getUri(), isBds).flatMap(transactionBuilder -> {
      if (isBds && transactionBuilder.getType()
          .equalsIgnoreCase(TransactionData.TransactionType.INAPP.name())) {
        return getCompletedPurchase(transaction.getPackageName(), transaction.getProductId()).map(
            purchase -> mapToBdsPayment(transaction, purchase))
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(payment -> remove(transaction.getUri()).toSingleDefault(payment));
      } else {
        return Single.fromCallable(() -> transaction)
            .flatMap(bundle -> remove(transaction.getUri()).toSingleDefault(bundle));
      }
    });
  }

  private Payment mapToBdsPayment(Payment transaction, Purchase purchase) {
    return new Payment(transaction.getUri(), transaction.getStatus(), purchase.getUid(),
        purchase.getSignature()
            .getValue(), billingSerializer.serializeSignatureData(purchase));
  }

  public Single<Boolean> isWalletFromBds(String packageName, String wallet) {
    if (packageName == null) {
      return Single.just(false);
    }
    return bdsInAppPurchaseInteractor.getWallet(packageName)
        .map(wallet::equalsIgnoreCase)
        .onErrorReturn(throwable -> false);
  }

  public Single<Gateway.Name> getPaymentMethod(String packageName,
      TransactionBuilder transactionBuilder) {
    return Single.zip(getRewardsBalance(), hasAppcoinsFunds(transactionBuilder),
        asfInAppPurchaseInteractor.getTransaction(packageName, transactionBuilder.getSkuId(),
            "inapp"),
        (creditsBalance, hasAppcoinsFunds, transaction) -> map(creditsBalance, hasAppcoinsFunds,
            transaction, transactionBuilder.amount()));
  }

  private Single<Boolean> hasAppcoinsFunds(TransactionBuilder transaction) {
    return asfInAppPurchaseInteractor.isAppcoinsPaymentReady(transaction);
  }

  private Gateway.Name map(BigDecimal creditsBalance, Boolean hasAppcoinsFunds,
      Transaction transaction, BigDecimal amount) {
    if (isTransactionOccurring(transaction)) {
      return transaction.getGateway()
          .getName();
    } else {
      return getNewPaymentGateway(creditsBalance, hasAppcoinsFunds, amount);
    }
  }

  private boolean isTransactionOccurring(Transaction transaction) {
    return transaction.getStatus()
        .equals(Transaction.Status.PROCESSING) || (transaction.getStatus()
        .equals(Transaction.Status.PENDING_SERVICE_AUTHORIZATION) && transaction.getGateway()
        .getName()
        .equals(Gateway.Name.appcoins));
  }

  private Gateway.Name getNewPaymentGateway(BigDecimal creditsBalance, Boolean hasAppcoinsFunds,
      BigDecimal amount) {
    if (creditsBalance.compareTo(amount) >= 0) {
      return Gateway.Name.appcoins_credits;
    } else if (hasAppcoinsFunds) {
      return Gateway.Name.appcoins;
    } else {
      return Gateway.Name.adyen;
    }
  }

  private Single<BigDecimal> getRewardsBalance() {
    return appcoinsRewards.getBalance()
        .map(BalanceUtils::weiToEth);
  }

  Single<String> getTransactionUid(String uid) {
    return transactionIdRepository.getTransactionUid(uid);
  }
}
