package com.asfoundation.wallet.ui.iab;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType;
import com.appcoins.wallet.core.network.microservices.model.FeeEntity;
import com.appcoins.wallet.core.network.microservices.model.FeeType;
import com.appcoins.wallet.core.network.microservices.model.Gateway;
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.appcoins.wallet.core.utils.properties.MiscProperties;
import com.appcoins.wallet.feature.backup.data.use_cases.ShouldShowSystemNotificationUseCase;
import com.appcoins.wallet.feature.backup.data.use_cases.UpdateWalletPurchasesCountUseCase;
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue;
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase;
import com.asf.wallet.R;
import com.asfoundation.wallet.backup.NotificationNeeded;
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel;
import com.asfoundation.wallet.billing.paypal.PaypalSupportedCurrencies;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
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
import javax.inject.Inject;
import javax.inject.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InAppPurchaseInteractor {

  public static final String PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY";
  private static final String LOCAL_PAYMENT_METHOD_KEY = "LOCAL_PAYMENT_METHOD_KEY";
  private static final String LAST_USED_PAYMENT_METHOD_KEY = "LAST_USED_PAYMENT_METHOD_KEY";
  private static final String APPC_ID = "appcoins";
  private static final String CREDITS_ID = "appcoins_credits";
  private static final long EARN_APPCOINS_APTOIDE_VERCODE = 9961;
  private final AsfInAppPurchaseInteractor asfInAppPurchaseInteractor;
  private final BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor;
  private final GetWalletInfoUseCase getWalletInfoUseCase;
  private final Billing billing;
  private final SharedPreferences sharedPreferences;
  private final PackageManager packageManager;
  private final ShouldShowSystemNotificationUseCase shouldShowSystemNotificationUseCase;
  private final UpdateWalletPurchasesCountUseCase updateWalletPurchasesCountUseCase;
  private final BillingMessagesMapper billingMessagesMapper;

  public @Inject InAppPurchaseInteractor(
      @Named("ASF_IN_APP_INTERACTOR") AsfInAppPurchaseInteractor asfInAppPurchaseInteractor,
      BdsInAppPurchaseInteractor bdsInAppPurchaseInteractor,
      GetWalletInfoUseCase getWalletInfoUseCase, Billing billing,
      SharedPreferences sharedPreferences, PackageManager packageManager,
      ShouldShowSystemNotificationUseCase shouldShowSystemNotificationUseCase,
      UpdateWalletPurchasesCountUseCase updateWalletPurchasesCountUseCase,
      BillingMessagesMapper billingMessagesMapper) {
    this.asfInAppPurchaseInteractor = asfInAppPurchaseInteractor;
    this.bdsInAppPurchaseInteractor = bdsInAppPurchaseInteractor;
    this.getWalletInfoUseCase = getWalletInfoUseCase;
    this.billing = billing;
    this.sharedPreferences = sharedPreferences;
    this.packageManager = packageManager;
    this.shouldShowSystemNotificationUseCase = shouldShowSystemNotificationUseCase;
    this.updateWalletPurchasesCountUseCase = updateWalletPurchasesCountUseCase;
    this.billingMessagesMapper = billingMessagesMapper;
  }

  public Single<NotificationNeeded> incrementAndValidateNotificationNeeded() {
    return getWalletInfoUseCase.invoke(null, true)
        .flatMap(walletInfo -> updateWalletPurchasesCountUseCase.invoke(walletInfo)
            .andThen(shouldShowSystemNotificationUseCase.invoke(walletInfo)
                .flatMap(needed -> Single.just(
                    new NotificationNeeded(needed, walletInfo.getWallet())))));
  }

  public Single<TransactionBuilder> parseTransaction(String uri, boolean isBds) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.parseTransaction(uri);
    } else {
      return asfInAppPurchaseInteractor.parseTransaction(uri);
    }
  }

  public Completable send(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload, boolean isBds,
      TransactionBuilder transactionBuilder) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
          developerPayload, transactionBuilder);
    } else {
      return asfInAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
          developerPayload, transactionBuilder);
    }
  }

  Completable resume(String uri, AsfInAppPurchaseInteractor.TransactionType transactionType,
      String packageName, String productName, String developerPayload, boolean isBds, String type,
      TransactionBuilder transactionBuilder) {
    if (isBds) {
      return bdsInAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
          developerPayload, type, transactionBuilder);
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

  public Single<FiatValue> convertFiatToLocalFiat(double value, String currency) {
    return asfInAppPurchaseInteractor.convertFiatToLocalFiat(value, currency);
  }

  public Single<FiatValue> convertFiatToAppc(double value, String currency) {
    return asfInAppPurchaseInteractor.convertFiatToAppc(value, currency);
  }

  public BillingMessagesMapper getBillingMessagesMapper() {
    return bdsInAppPurchaseInteractor.getBillingMessagesMapper();
  }

  private Single<Purchase> getCompletedPurchase(String packageName, String productName,
      String purchaseUid, String type) {
    return bdsInAppPurchaseInteractor.getCompletedPurchase(packageName, productName, purchaseUid,
        type);
  }

  Single<Payment> getCompletedPurchase(Payment transaction, boolean isBds) {
    return parseTransaction(transaction.getUri(), isBds).flatMap(transactionBuilder -> {
      if (isBds && transactionBuilder.getType()
          .equalsIgnoreCase(TransactionData.TransactionType.INAPP.name())) {
        return getCompletedPurchase(transaction.getPackageName(), transaction.getProductId(),
            transaction.getPurchaseUid(), transactionBuilder.getType()).map(
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
        transaction.getPurchaseUid(), purchase.getSignature()
        .getValue(), purchase.getSignature()
        .getMessage(), transaction.getOrderReference(), transaction.getErrorCode(),
        transaction.getErrorMessage());
  }

  public Single<Boolean> isWalletFromBds(String packageName, String wallet) {
    if (packageName == null) {
      return Single.just(false);
    }
    return bdsInAppPurchaseInteractor.getWallet(packageName)
        .map(wallet::equalsIgnoreCase)
        .onErrorReturn(throwable -> false);
  }

  // uncomment to reactivate gas_price on payment flow:
  //private Single<List<Gateway.Name>> getFilteredGateways(TransactionBuilder transactionBuilder) {
  //  return Single.zip(getRewardsBalance(), hasAppcoinsFunds(transactionBuilder),
  //      (creditsBalance, hasAppcoinsFunds) -> getNewPaymentGateways(creditsBalance,
  //          hasAppcoinsFunds, transactionBuilder.amount()));
  //}

  private Single<List<Gateway.Name>> getFilteredGateways(TransactionBuilder transactionBuilder) {
    return getRewardsBalance().map(creditsBalance -> getNewPaymentGateways(creditsBalance, false,
        transactionBuilder.amount()));
  }

  //public Single<Boolean> hasAppcoinsFunds(TransactionBuilder transaction) {
  //  return asfInAppPurchaseInteractor.isAppcoinsPaymentReady(transaction);
  //}
  //

  public Single<InAppPurchaseService.BalanceState> getBalanceState(TransactionBuilder transaction) {
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
    return getWalletInfoUseCase.invoke(null, true)
        .map(walletInfo -> walletInfo.getWalletBalance()
            .getCreditsBalance()
            .getToken()
            .getAmount());
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
            transaction.getType(), transaction.getDomain())
        .flatMap(paymentMethods -> getAvailablePaymentMethods(transaction, paymentMethods).flatMap(
                availablePaymentMethods -> Observable.fromIterable(paymentMethods)
                    .map(paymentMethod -> mapPaymentMethods(paymentMethod, availablePaymentMethods,
                        currency))
                    .flatMap(paymentMethod -> retrieveDisableReason(paymentMethod, transaction))
                    .toList())
            .map(this::removePaymentMethods))
        .map(this::swapDisabledPositions)
        .map(this::showTopup);
  }

  private List<PaymentMethod> showTopup(List<PaymentMethod> paymentMethods) {
    if (paymentMethods.size() == 0) {
      return paymentMethods;
    }

    int appcCreditPaymentIndex = 0;
    for (int i = 0; i < paymentMethods.size(); i++) {
      PaymentMethod paymentMethod = paymentMethods.get(i);
      if (paymentMethod.isEnabled()) {
        return paymentMethods;
      }
      if (paymentMethod.getId()
          .equals(CREDITS_ID)) {
        appcCreditPaymentIndex = i;
      }
    }
    PaymentMethod appcPaymentMethod = paymentMethods.get(appcCreditPaymentIndex);
    paymentMethods.set(appcCreditPaymentIndex,
        new PaymentMethod(appcPaymentMethod.getId(), appcPaymentMethod.getLabel(),
            appcPaymentMethod.getIconUrl(), appcPaymentMethod.getAsync(),
            appcPaymentMethod.getFee(), appcPaymentMethod.isEnabled(),
            appcPaymentMethod.getDisabledReason(), true, false, false));
    return paymentMethods;
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
    Iterator<PaymentMethod> iterator = paymentMethods.iterator();
    while (iterator.hasNext()) {
      PaymentMethod paymentMethod = iterator.next();
      if (paymentMethod.getId()
          .equals("earn_appcoins")) {
        iterator.remove();
      }
    }
    return paymentMethods;
  }

  private boolean hasRequiredAptoideVersionInstalled() {
    try {
      PackageInfo packageInfo =
          packageManager.getPackageInfo(MiscProperties.INSTANCE.getAPTOIDE_PKG_NAME(), 0);
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
    boolean addedMergedAppc = false;
    for (PaymentMethod paymentMethod : paymentMethods) {
      if ((paymentMethod.getId()
          .equals(APPC_ID) || paymentMethod.getId()
          .equals(CREDITS_ID)) && !addedMergedAppc) {
        String mergedId = "merged_appcoins";
        String mergedLabel = creditsMethod.getLabel() + " / " + appcMethod.getLabel();
        boolean isMergedEnabled = appcMethod.isEnabled() || creditsMethod.isEnabled();
        Integer disableReason = mergeDisableReason(appcMethod, creditsMethod);
        mergedList.add(new AppCoinsPaymentMethod(mergedId, mergedLabel, appcMethod.getIconUrl(),
            isMergedEnabled, appcMethod.isEnabled(), creditsMethod.isEnabled(),
            appcMethod.getLabel(), creditsMethod.getLabel(), creditsMethod.getIconUrl(),
            disableReason, appcMethod.getDisabledReason(), creditsMethod.getDisabledReason()));
        addedMergedAppc = true;
      } else if (!paymentMethod.getId()
          .equals(CREDITS_ID) && !paymentMethod.getId()
          .equals(APPC_ID)) {
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
      } else if (paymentMethod.getGateway() != null &&
          (paymentMethod.getGateway().getName() == (Gateway.Name.myappcoins)
              || paymentMethod.getGateway().getName() == (Gateway.Name.adyen_v2)
              || paymentMethod.getGateway().getName() == Gateway.Name.challenge_reward
          ) && !paymentMethod.isAvailable()) {
        iterator.remove();
      }
    }
    return clonedPaymentMethods;
  }

  private PaymentMethod mapPaymentMethods(PaymentMethodEntity paymentMethod,
      List<PaymentMethodEntity> availablePaymentMethods, String currency) {
    for (PaymentMethodEntity availablePaymentMethod : availablePaymentMethods) {
      if (paymentMethod.getId()
          .equals(availablePaymentMethod.getId())) {
        PaymentMethodFee paymentMethodFee = mapPaymentMethodFee(availablePaymentMethod.getFee());
        return new PaymentMethod(paymentMethod.getId(), paymentMethod.getLabel(),
            paymentMethod.getIconUrl(), paymentMethod.getAsync(), paymentMethodFee, true, null,
            false, isToShowPaypalLogout(paymentMethod), hasExtraFees(paymentMethod, currency));
      }
    }
    PaymentMethodFee paymentMethodFee = mapPaymentMethodFee(paymentMethod.getFee());
    return new PaymentMethod(paymentMethod.getId(), paymentMethod.getLabel(),
        paymentMethod.getIconUrl(), paymentMethod.getAsync(), paymentMethodFee, false, null, false,
        isToShowPaypalLogout(paymentMethod), hasExtraFees(paymentMethod, currency));
  }

  private Boolean isToShowPaypalLogout(PaymentMethodEntity paymentMethod) {
    return paymentMethod.getId()
        .equals(PaymentMethodsView.PaymentMethodId.PAYPAL_V2.getId());
  }

  private Boolean hasExtraFees(PaymentMethodEntity paymentMethod, String currency) {
    return (paymentMethod.getId()
        .equals(PaymentMethodsView.PaymentMethodId.PAYPAL_V2.getId())
        && !PaypalSupportedCurrencies.Companion.getCurrencies()
        .contains(currency));
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

  @NotNull public Single<PurchaseBundleModel> getCompletedPurchaseBundle(@NotNull String type,
      @NotNull String merchantName, @Nullable String sku, @Nullable String purchaseUid,
      @Nullable String orderReference, @Nullable String hash, @NotNull Scheduler scheduler) {
    BillingSupportedType billingType = BillingSupportedType.valueOfInsensitive(type);
    if (isManagedTransaction(billingType) && sku != null) {
      return billing.getSkuPurchase(merchantName, sku, purchaseUid, scheduler, billingType)
          .map(purchase -> new PurchaseBundleModel(
              billingMessagesMapper.mapPurchase(purchase, orderReference), purchase.getRenewal()));
    } else {
      return Single.just(new PurchaseBundleModel(billingMessagesMapper.successBundle(hash), null));
    }
  }

  private Boolean isManagedTransaction(BillingSupportedType type) {
    return type == BillingSupportedType.INAPP || type == BillingSupportedType.INAPP_SUBSCRIPTION;
  }
}