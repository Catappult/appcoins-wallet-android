package com.asfoundation.wallet.ui.iab;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer;
import com.appcoins.wallet.bdsbilling.repository.entity.FeeEntity;
import com.appcoins.wallet.bdsbilling.repository.entity.FeeType;
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway;
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.R;
import com.asfoundation.wallet.backup.BackupInteractContract;
import com.asfoundation.wallet.backup.NotificationNeeded;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.GetDefaultWalletBalanceInteract;
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

  public static final String PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY";
  private static final String LOCAL_PAYMENT_METHOD_KEY = "LOCAL_PAYMENT_METHOD_KEY";
  private static final String LAST_USED_PAYMENT_METHOD_KEY = "LAST_USED_PAYMENT_METHOD_KEY";
  private static final String APPC_ID = "appcoins";
  private static final String CREDITS_ID = "appcoins_credits";
  private static final long EARN_APPCOINS_APTOIDE_VERCODE = 9961;
  private final AsfInAppPurchaseInteractor asfInAppPurchaseInteractor;
  private final BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor;
  private final ExternalBillingSerializer billingSerializer;
  private final AppcoinsRewards appcoinsRewards;
  private final Billing billing;
  private final SharedPreferences sharedPreferences;
  private final PackageManager packageManager;
  private final BackupInteractContract backupInteract;

  public InAppPurchaseInteractor(AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      ExternalBillingSerializer billingSerializer, AppcoinsRewards appcoinsRewards, Billing billing,
      SharedPreferences sharedPreferences, PackageManager packageManager,
      BackupInteractContract backupInteract) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
    this.billingSerializer = billingSerializer;
    this.appcoinsRewards = appcoinsRewards;
    this.billing = billing;
    this.sharedPreferences = sharedPreferences;
    this.packageManager = packageManager;
    this.backupInteract = backupInteract;
  }

  public Single<NotificationNeeded> incrementAndValidateNotificationNeeded() {
    return asfInAppPurchaseInteractor.getWalletAddress()
        .flatMap(walletAddress -> backupInteract.updateWalletPurchasesCount(walletAddress)
            .andThen(shouldShowSystemNotification(walletAddress).map(
                needed -> new NotificationNeeded(needed, walletAddress))));
  }

  private Single<Boolean> shouldShowSystemNotification(String walletAddress) {
    return Single.create(emitter -> {
      boolean shouldShow = backupInteract.shouldShowSystemNotification(walletAddress);
      emitter.onSuccess(shouldShow);
    });
  }

  public Single<TransactionBuilder> parseTransaction(String uri, boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.parseTransaction(uri);
    } else {
      return asfInAppPurchaseInteractor.parseTransaction(uri);
    }
  }

  public Completable send(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload, boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
          developerPayload);
    } else {
      return asfInAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
          developerPayload);
    }
  }

  Completable resume(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload, boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
          developerPayload);
    } else {
      return Completable.error(new UnsupportedOperationException("Asf doesn't support resume."));
    }
  }

  Observable<Payment> getTransactionState(String uri) {
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

  List<BigDecimal> getTopUpChannelSuggestionValues(BigDecimal price) {
    return bdsInAppPurchaseInteractor.getTopUpChannelSuggestionValues(price);
  }

  public Single<String> getWalletAddress() {
    return asfInAppPurchaseInteractor.getWalletAddress();
  }

  Single<AsfInAppPurchaseInteractor.CurrentPaymentStep> getCurrentPaymentStep(String packageName,
      TransactionBuilder transactionBuilder) {
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

  private Single<Purchase> getCompletedPurchase(String packageName, String productName) {
    return bdsInAppPurchaseInteractor.getCompletedPurchase(packageName, productName);
  }

  Single<Payment> getCompletedPurchase(Payment transaction, boolean isBds) {
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
        transaction.getOrderReference(), transaction.getErrorCode(), transaction.getErrorMessage());
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

  public Single<Boolean> hasAppcoinsFunds(TransactionBuilder transaction) {
    return asfInAppPurchaseInteractor.isAppcoinsPaymentReady(transaction);
  }

  public Single<GetDefaultWalletBalanceInteract.BalanceState> getBalanceState(
      TransactionBuilder transaction) {
    return asfInAppPurchaseInteractor.getAppcoinsBalanceState(transaction);
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

    list.add(Gateway.Name.adyen_v2);

    return list;
  }

  private Single<BigDecimal> getRewardsBalance() {
    return appcoinsRewards.getBalance()
        .map(BalanceUtils::weiToEth);
  }

  private Single<List<PaymentMethodEntity>> getAvailablePaymentMethods(
      TransactionBuilder transaction, List<PaymentMethodEntity> paymentMethods) {
    return getFilteredGateways(transaction).map(
        filteredGateways -> removeUnavailable(paymentMethods, filteredGateways));
  }

  public Observable<Transaction> getTransaction(String uid) {
    return Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.io())
        .timeInterval()
        .switchMap(longTimed -> billing.getAppcoinsTransaction(uid, Schedulers.io())
            .toObservable());
  }

  Single<List<PaymentMethod>> getPaymentMethods(TransactionBuilder transaction,
      String transactionValue, String currency) {
    return bdsInAppPurchaseInteractor.getPaymentMethods(transactionValue, currency,
        transaction.getType())
        .flatMap(paymentMethods -> getAvailablePaymentMethods(transaction, paymentMethods).flatMap(
            availablePaymentMethods -> Observable.fromIterable(paymentMethods)
                .map(paymentMethod -> mapPaymentMethods(paymentMethod, availablePaymentMethods))
                .flatMap(paymentMethod -> retrieveDisableReason(paymentMethod, transaction))
                .toList())
            .map(this::removePaymentMethods))
        .map(this::swapDisabledPositions);
  }

  private Observable<PaymentMethod> retrieveDisableReason(PaymentMethod paymentMethod,
      TransactionBuilder transaction) {
    if (!paymentMethod.isEnabled()) {
      if (paymentMethod.getId()
          .equals(CREDITS_ID)) {
        paymentMethod.setDisabledReason(R.string.purchase_appcoins_credits_noavailable_body);
      } else if (paymentMethod.getId()
          .equals(APPC_ID)) {
        return getAppcDisableReason(transaction).filter(reason -> reason != -1)
            .map(reason -> {
              paymentMethod.setDisabledReason(reason);
              return paymentMethod;
            });
      }
    }
    return Observable.just(paymentMethod);
  }

  private Observable<Integer> getAppcDisableReason(TransactionBuilder transaction) {
    return getBalanceState(transaction).map(balanceState -> {
      switch (balanceState) {
        case NO_ETHER:
          return R.string.purchase_no_eth_body;
        case NO_TOKEN:
        case NO_ETHER_NO_TOKEN:
          return R.string.purchase_no_appcoins_body;
        case OK:
        default:
          return -1;
      }
    })
        .toObservable();
  }

  private List<PaymentMethod> removePaymentMethods(List<PaymentMethod> paymentMethods) {
    if (hasFunds(paymentMethods) || !hasRequiredAptoideVersionInstalled()) {
      Iterator<PaymentMethod> iterator = paymentMethods.iterator();
      while (iterator.hasNext()) {
        PaymentMethod paymentMethod = iterator.next();
        if (paymentMethod.getId()
            .equals("earn_appcoins")) {
          iterator.remove();
        }
      }
    }
    return paymentMethods;
  }

  private boolean hasRequiredAptoideVersionInstalled() {
    try {
      PackageInfo packageInfo = packageManager.getPackageInfo(BuildConfig.APTOIDE_PKG_NAME, 0);
      return packageInfo.versionCode >= EARN_APPCOINS_APTOIDE_VERCODE;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean hasFunds(List<PaymentMethod> clonedList) {
    for (PaymentMethod paymentMethod : clonedList) {
      if ((paymentMethod.getId()
          .equals(APPC_ID) && paymentMethod.isEnabled())
          || paymentMethod.getId()
          .equals(CREDITS_ID) && paymentMethod.isEnabled()) {
        return true;
      }
    }
    return false;
  }

  List<PaymentMethod> mergeAppcoins(List<PaymentMethod> paymentMethods) {
    PaymentMethod appcMethod = getAppcMethod(paymentMethods);
    PaymentMethod creditsMethod = getCreditsMethod(paymentMethods);
    if (appcMethod != null && creditsMethod != null) {
      return buildMergedList(paymentMethods, appcMethod, creditsMethod);
    }
    return paymentMethods;
  }

  private List<PaymentMethod> buildMergedList(List<PaymentMethod> paymentMethods,
      PaymentMethod appcMethod, PaymentMethod creditsMethod) {
    List<PaymentMethod> mergedList = new ArrayList<>();
    for (PaymentMethod paymentMethod : paymentMethods) {
      if (paymentMethod.getId()
          .equals(APPC_ID)) {
        String mergedId = "merged_appcoins";
        String mergedLabel = appcMethod.getLabel() + " / " + creditsMethod.getLabel();
        boolean isMergedEnabled = appcMethod.isEnabled() || creditsMethod.isEnabled();
        Integer disableReason = mergeDisableReason(appcMethod, creditsMethod);
        mergedList.add(new AppCoinsPaymentMethod(mergedId, mergedLabel, appcMethod.getIconUrl(),
            isMergedEnabled, appcMethod.isEnabled(), creditsMethod.isEnabled(),
            appcMethod.getLabel(), creditsMethod.getLabel(), creditsMethod.getIconUrl(),
            disableReason, appcMethod.getDisabledReason(), creditsMethod.getDisabledReason()));
      } else if (!paymentMethod.getId()
          .equals(CREDITS_ID)) {
        //Don't add the credits method to this list
        mergedList.add(paymentMethod);
      }
    }
    return mergedList;
  }

  private Integer mergeDisableReason(PaymentMethod appcMethod, PaymentMethod creditsMethod) {
    Integer creditsReason = creditsMethod.getDisabledReason();
    Integer appcReason = appcMethod.getDisabledReason();
    if (creditsReason == null) {
      creditsReason = -1;
    }
    if (appcReason == null) {
      appcReason = -1;
    }
    if (!creditsMethod.isEnabled() && !appcMethod.isEnabled()) {
      // Specific cases that are treated differently:
      // - If user does not have APPC-C, has APPC, but no ETH, the message should be to display
      //    that user does not have enough ETH (may have none, or some but not enough)
      // - If user does not have APPC-C nor APPC (ETH value doesn't matter),
      //    the message should be more generic, indicating that user does not have funds
      return (appcReason == R.string.purchase_no_eth_body) ? appcReason
          : R.string.p2p_send_error_not_enough_funds;
    }
    if (!creditsMethod.isEnabled()) {
      return (creditsReason != -1) ? creditsReason : appcReason;
    }
    if (!appcMethod.isEnabled()) {
      return (appcReason != -1) ? appcReason : creditsReason;
    }
    return null;
  }

  private PaymentMethod getCreditsMethod(List<PaymentMethod> paymentMethods) {
    for (PaymentMethod paymentMethod : paymentMethods) {
      if (paymentMethod.getId()
          .equals(CREDITS_ID)) {
        return paymentMethod;
      }
    }
    return null;
  }

  private PaymentMethod getAppcMethod(List<PaymentMethod> paymentMethods) {
    for (PaymentMethod paymentMethod : paymentMethods) {
      if (paymentMethod.getId()
          .equals(APPC_ID)) {
        return paymentMethod;
      }
    }
    return null;
  }

  public List<PaymentMethod> swapDisabledPositions(List<PaymentMethod> paymentMethods) {
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
      if (id.equals(APPC_ID) && !filteredGateways.contains(Gateway.Name.appcoins)) {
        iterator.remove();
      } else if (id.equals(CREDITS_ID) && !filteredGateways.contains(
          Gateway.Name.appcoins_credits)) {
        iterator.remove();
      } else if (paymentMethod.getGateway() != null && (paymentMethod.getGateway()
          .getName() == (Gateway.Name.myappcoins)
          || paymentMethod.getGateway()
          .getName() == (Gateway.Name.adyen_v2)) && !paymentMethod.isAvailable()) {
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
        PaymentMethodFee paymentMethodFee = mapPaymentMethodFee(availablePaymentMethod.getFee());
        return new PaymentMethod(paymentMethod.getId(), paymentMethod.getLabel(),
            paymentMethod.getIconUrl(), paymentMethod.getAsync(), paymentMethodFee, true, null);
      }
    }
    PaymentMethodFee paymentMethodFee = mapPaymentMethodFee(paymentMethod.getFee());
    return new PaymentMethod(paymentMethod.getId(), paymentMethod.getLabel(),
        paymentMethod.getIconUrl(), paymentMethod.getAsync(), paymentMethodFee, false, null);
  }

  private PaymentMethodFee mapPaymentMethodFee(FeeEntity feeEntity) {
    if (feeEntity == null) {
      return null;
    } else {
      if (feeEntity.getType() == FeeType.EXACT) {
        return new PaymentMethodFee(true, feeEntity.getCost()
            .getValue(), feeEntity.getCost()
            .getCurrency());
      } else {
        return new PaymentMethodFee(false, null, null);
      }
    }
  }

  boolean hasPreSelectedPaymentMethod() {
    return sharedPreferences.contains(PRE_SELECTED_PAYMENT_METHOD_KEY);
  }

  String getPreSelectedPaymentMethod() {
    return sharedPreferences.getString(PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.APPC_CREDITS.getId());
  }

  boolean hasAsyncLocalPayment() {
    return sharedPreferences.contains(LOCAL_PAYMENT_METHOD_KEY);
  }

  public void savePreSelectedPaymentMethod(String paymentMethod) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(PRE_SELECTED_PAYMENT_METHOD_KEY, paymentMethod);
    editor.putString(LAST_USED_PAYMENT_METHOD_KEY, paymentMethod);
    editor.apply();
  }

  public void saveAsyncLocalPayment(String paymentMethod) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(LOCAL_PAYMENT_METHOD_KEY, paymentMethod);
    editor.apply();
  }

  public void removePreSelectedPaymentMethod() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove(PRE_SELECTED_PAYMENT_METHOD_KEY);
    editor.apply();
  }

  public void removeAsyncLocalPayment() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove(LOCAL_PAYMENT_METHOD_KEY);
    editor.apply();
  }

  String getLastUsedPaymentMethod() {
    return sharedPreferences.getString(LAST_USED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.CREDIT_CARD.getId());
  }
}
