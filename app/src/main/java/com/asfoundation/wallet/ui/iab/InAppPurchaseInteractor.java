package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway;
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity;
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
import java.util.ArrayList;
import java.util.Collections;
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
  private final PaymentMethodsMapper paymentMethodsMapper;

  public InAppPurchaseInteractor(AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      ExternalBillingSerializer billingSerializer, AppcoinsRewards appcoinsRewards, Billing billing,
      PaymentMethodsMapper paymentMethodsMapper) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
    this.billingSerializer = billingSerializer;
    this.appcoinsRewards = appcoinsRewards;
    this.billing = billing;
    this.paymentMethodsMapper = paymentMethodsMapper;
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

  public Single<Transaction> getCompletedTransaction(String packageName, String productName,
      String type) {
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

  private Single<List<Gateway.Name>> getFilteredGateways(TransactionBuilder transactionBuilder) {
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
    return getCompletedTransaction(uid).map(transaction -> transaction.getHash())
        .firstOrError();
  }

  public Single<Double> getTransactionAmount(String uid) {
    return getCompletedTransaction(uid).map(transaction -> Double.parseDouble(transaction.getPrice()
        .getAppc()))
        .firstOrError();
  }

  private Single<List<PaymentMethodEntity>> getAvailablePaymentMethods(
      TransactionBuilder transaction, List<PaymentMethodEntity> paymentMethods) {
    return getFilteredGateways(transaction).map(
        filteredGateways -> removeUnavailable(paymentMethods, filteredGateways));
  }

  private Observable<Transaction> getCompletedTransaction(String uid) {
    return getTransaction(uid).filter(transaction -> transaction.getStatus()
        .equals(Status.COMPLETED));
  }

  public Observable<Transaction> getTransaction(String uid) {
    return Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.io())
        .timeInterval()
        .switchMap(longTimed -> billing.getAppcoinsTransaction(uid, Schedulers.io())
            .toObservable());
  }

  public Single<List<PaymentMethod>> getPaymentMethods(TransactionBuilder transaction,
      String transactionValue, String currency) {
    return bdsInAppPurchaseInteractor.getPaymentMethods(transactionValue, currency)
        .flatMap(paymentMethods -> getAvailablePaymentMethods(transaction, paymentMethods).flatMap(
            availablePaymentMethods -> Observable.fromIterable(paymentMethods)
                .map(paymentMethod -> mapPaymentMethods(paymentMethod, availablePaymentMethods))
                .toList()))
        .map(this::swapDisabledPositions);
  }

  private List<PaymentMethod> swapDisabledPositions(List<PaymentMethod> paymentMethods) {
    boolean swapped = false;
    if (paymentMethods.size() > 1) {
      for (int position = 1; position < paymentMethods.size(); position++) {
        if (shouldSwap(paymentMethods, position)) {
          Collections.swap(paymentMethods, position, position - 1);
          swapped = true;
          break;
        }
      }
      if (swapped) {
        swapDisabledPositions(paymentMethods);
      }
    }
    return paymentMethods;
  }

  private boolean shouldSwap(List<PaymentMethod> paymentMethods, int position) {
    return paymentMethods.get(position)
        .isEnabled() && !paymentMethods.get(position - 1)
        .isEnabled();
  }

  private List<PaymentMethodEntity> removeUnavailable(List<PaymentMethodEntity> paymentMethods,
      List<Gateway.Name> filteredGateways) {
    List<PaymentMethodEntity> clonedPaymentMethods = new ArrayList<>(paymentMethods);
    Iterator<PaymentMethodEntity> iterator = clonedPaymentMethods.iterator();

    while (iterator.hasNext()) {
      PaymentMethodEntity paymentMethod = iterator.next();
      String id = paymentMethod.getId();
      if (id.equals("appcoins") && !filteredGateways.contains(Gateway.Name.appcoins)) {
        iterator.remove();
      } else if (id.equals("appcoins_credits") && !filteredGateways.contains(
          Gateway.Name.appcoins_credits)) {
        iterator.remove();
      } else if (paymentMethod.getGateway()
          .getName() == (Gateway.Name.myappcoins)
          && paymentMethod.getAvailability() != null
          && paymentMethod.getAvailability()
          .equals("UNAVAILABLE")) {
        iterator.remove();
      }
    }
    return clonedPaymentMethods;
  }

  private PaymentMethod mapPaymentMethods(PaymentMethodEntity paymentMethod,
      List<PaymentMethodEntity> availablePaymentMethods) {
    for (PaymentMethodEntity availablePaymentMethod : availablePaymentMethods) {
      if (paymentMethod.getId()
          .equals(availablePaymentMethod.getId())) {
        return new PaymentMethod(paymentMethodsMapper.map(paymentMethod.getId()),
            paymentMethod.getLabel(), paymentMethod.getIconUrl(), true);
      }
    }
    return new PaymentMethod(paymentMethodsMapper.map(paymentMethod.getId()),
        paymentMethod.getLabel(), paymentMethod.getIconUrl(), false);
  }
}
