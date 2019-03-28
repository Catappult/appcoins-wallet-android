package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway;
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethod;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.BalanceUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InAppPurchaseInteractor {

  private final AsfInAppPurchaseInteractor asfInAppPurchaseInteractor;
  private final BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor;
  private final ExternalBillingSerializer billingSerializer;
  private final AppcoinsRewards appcoinsRewards;
  private final Billing billing;

  public InAppPurchaseInteractor(AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      ExternalBillingSerializer billingSerializer, AppcoinsRewards appcoinsRewards,
      Billing billing) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
    this.billingSerializer = billingSerializer;
    this.appcoinsRewards = appcoinsRewards;
    this.billing = billing;
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

  public Single<FiatValue> convertToLocalFiat(double appcValue) {
    return asfInAppPurchaseInteractor.convertToLocalFiat(appcValue);
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
            .getValue(), billingSerializer.serializeSignatureData(purchase),
        transaction.getOrderReference());
  }

  public Single<Boolean> isWalletFromBds(String packageName, String wallet) {
    if (packageName == null) {
      return Single.just(false);
    }
    return bdsInAppPurchaseInteractor.getWallet(packageName)
        .map(wallet::equalsIgnoreCase)
        .onErrorReturn(throwable -> false);
  }

  public Single<List<Gateway.Name>> getFilteredGateways(TransactionBuilder transactionBuilder) {
    return Single.zip(getRewardsBalance(), hasAppcoinsFunds(transactionBuilder),
        (creditsBalance, hasAppcoinsFunds) -> getNewPaymentGateways(creditsBalance,
            hasAppcoinsFunds, transactionBuilder.amount()));
  }

  private Single<Boolean> hasAppcoinsFunds(TransactionBuilder transaction) {
    return asfInAppPurchaseInteractor.isAppcoinsPaymentReady(transaction);
  }

  private List<Gateway.Name> getNewPaymentGateways(BigDecimal creditsBalance,
      Boolean hasAppcoinsFunds, BigDecimal amount) {
    List<Gateway.Name> list = new LinkedList<>();

    if (creditsBalance.compareTo(amount) >= 0) {
      list.add(Gateway.Name.appcoins_credits);
    }

    if (hasAppcoinsFunds) {
      list.add(Gateway.Name.appcoins);
    }

    list.add(Gateway.Name.adyen);

    return list;
  }

  private Single<BigDecimal> getRewardsBalance() {
    return appcoinsRewards.getBalance()
        .map(BalanceUtils::weiToEth);
  }

  public Single<String> getTransactionUid(String uid) {
    return getTransaction(uid).map(transaction -> transaction.getHash())
        .firstOrError();
  }

  public Single<Double> getTransactionAmount(String uid) {
    return getTransaction(uid).map(transaction -> Double.parseDouble(transaction.getPrice()
        .getAppc()))
        .firstOrError();
  }

  public Single<List<PaymentMethod>> getAvailablePaymentMethods(TransactionBuilder transaction) {
    return getPaymentMethods().flatMap(
        paymentMethods -> getFilteredGateways(transaction).map(filteredGateways -> {
          removeUnavailable(paymentMethods, filteredGateways);
          return paymentMethods;
        }));
  }

  private Observable<Transaction> getTransaction(String uid) {
    return Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.io())
        .timeInterval()
        .switchMap(longTimed -> billing.getAppcoinsTransaction(uid, Schedulers.io())
            .toObservable())
        .filter(transaction -> transaction.getStatus()
            .equals(Status.COMPLETED));
  }

  public Single<List<PaymentMethod>> getPaymentMethods() {
    return bdsInAppPurchaseInteractor.getPaymentMethods();
  }

  private void removeUnavailable(List<PaymentMethod> paymentMethods,
      List<Gateway.Name> filteredGateways) {
    Iterator<PaymentMethod> iterator = paymentMethods.iterator();

    while (iterator.hasNext()) {
      PaymentMethod paymentMethod = iterator.next();

      String id = paymentMethod.getId();
      if (id.equals("appcoins") && !filteredGateways.contains(Gateway.Name.appcoins)) {
        iterator.remove();
      } else if (id.equals("appcoins_credits") && !filteredGateways.contains(
          Gateway.Name.appcoins_credits)) {
        iterator.remove();
      }
    }
  }
}
