package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adyen.core.models.PaymentMethod;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asfoundation.wallet.billing.BillingService;
import com.asfoundation.wallet.billing.adyen.Adyen;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY;
import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_CC;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public class AdyenAuthorizationPresenter {

  private static final String WAITING_RESULT = "WAITING_RESULT";

  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final Adyen adyen;
  private final BillingService billingService;
  private final Navigator navigator;
  private final BillingMessagesMapper billingMessagesMapper;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final String transactionData;
  private final String developerPayload;
  private final Billing billing;
  private final String skuId;
  private final String type;
  private final String origin;
  private final String amount;
  private final String currency;
  private final String appPackage;
  private final PaymentType paymentType;
  private AdyenAuthorizationView view;
  private FindDefaultWalletInteract defaultWalletInteract;
  private BillingAnalytics analytics;
  private final Single<TransactionBuilder> transactionBuilder;
  private boolean waitingResult;
  private Scheduler ioScheduler;

  public AdyenAuthorizationPresenter(AdyenAuthorizationView view, String appPackage,
      FindDefaultWalletInteract defaultWalletInteract, Scheduler viewScheduler,
      CompositeDisposable disposables, Adyen adyen, BillingService billingService,
      Navigator navigator, BillingMessagesMapper billingMessagesMapper,
      InAppPurchaseInteractor inAppPurchaseInteractor, String transactionData,
      String developerPayload, Billing billing, String skuId, String type, String origin,
      String amount, String currency, PaymentType paymentType, BillingAnalytics analytics,
      Scheduler ioScheduler) {
    this.view = view;
    this.appPackage = appPackage;
    this.defaultWalletInteract = defaultWalletInteract;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.adyen = adyen;
    this.billingService = billingService;
    this.navigator = navigator;
    this.billingMessagesMapper = billingMessagesMapper;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.transactionData = transactionData;
    this.developerPayload = developerPayload;
    this.billing = billing;
    this.skuId = skuId;
    this.type = type;
    this.origin = origin;
    this.amount = amount;
    this.currency = currency;
    this.paymentType = paymentType;
    this.analytics = analytics;
    this.transactionBuilder = inAppPurchaseInteractor.parseTransaction(transactionData, true);
    this.ioScheduler = ioScheduler;
  }

  public void present(@Nullable Bundle savedInstanceState) {
    adyen.createNewPayment();

    if (savedInstanceState != null) {
      waitingResult = savedInstanceState.getBoolean(WAITING_RESULT);
    }

    disposables.add(defaultWalletInteract.find()
        .observeOn(viewScheduler)
        .doOnSuccess(wallet -> view.showWalletAddress(wallet.address))
        .subscribe(wallet -> {
        }, this::showError));

    onViewCreatedCompletePayment();

    onViewCreatedSelectPaymentMethod();

    onViewCreatedShowPaymentMethodInputView();

    onViewCreatedCheckAuthorizationActive();

    onViewCreatedCheckAuthorizationFailed();

    onViewCreatedCheckAuthorizationProcessing();

    handlePaymentMethodResults();

    handleChangeCardMethodResults();

    handleAdyenUriRedirect();

    handleAdyenUriResult();

    handleErrorDismissEvent();

    handleAdyenPaymentResult();

    handleCancel();
  }

  private void onViewCreatedShowPaymentMethodInputView() {
    disposables.add(adyen.getPaymentRequest()
        .filter(paymentRequest -> paymentRequest.getPaymentMethod() != null)
        .map(paymentRequest -> paymentRequest.getPaymentMethod()
            .getType())
        .distinctUntilChanged(
            (paymentRequest, paymentRequest2) -> paymentRequest.equals(paymentRequest2))
        .flatMapMaybe(type -> adyen.getPaymentRequest()
            .firstElement())
        .observeOn(viewScheduler)
        .doOnNext(data -> {
          if (data.getPaymentMethod()
              .getType()
              .equals(PaymentMethod.Type.CARD)) {
            view.showCreditCardView(data.getPaymentMethod(), data.getAmount(), true,
                data.getShopperReference() != null, data.getPublicKey(), data.getGenerationTime());
          } else {
            view.showCvcView(data.getAmount(), data.getPaymentMethod());
          }
        })
        .observeOn(viewScheduler)
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void showError(Throwable throwable) {
    throwable.printStackTrace();

    if (throwable instanceof IOException) {
      view.hideLoading();
      view.showNetworkError();
    } else {
      view.showGenericError();
    }
  }

  private void onViewCreatedCompletePayment() {
    disposables.add(Completable.fromAction(() -> view.showLoading())
        .andThen(transactionBuilder.flatMapCompletable(
            transaction -> billingService.getAuthorization(transaction.getSkuId(),
                transaction.toAddress(), developerPayload, origin, convertAmount(currency),
                currency, type, transaction.getCallbackUrl(), transaction.getOrderReference(),
                appPackage)
                .observeOn(viewScheduler)
                .filter(payment -> payment.isPendingAuthorization())
                .firstOrError()
                .map(payment -> payment)
                .flatMapCompletable(
                    authorization -> adyen.completePayment(authorization.getSession()))
                .observeOn(viewScheduler)))
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  @NonNull private BigDecimal convertAmount(String currency) {
    return inAppPurchaseInteractor.convertToLocalFiat((new BigDecimal(amount)).doubleValue())
        .subscribeOn(ioScheduler)
        .blockingGet()
        .getAmount()
        .setScale(2, BigDecimal.ROUND_UP);
  }

  private void onViewCreatedSelectPaymentMethod() {
    disposables.add(adyen.getPaymentMethod(paymentType)
        .flatMapCompletable(paymentMethod -> adyen.selectPaymentService(paymentMethod))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void onViewCreatedCheckAuthorizationActive() {
    disposables.add(transactionBuilder.flatMap(
        transaction -> billingService.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(currency), currency,
            type, transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage)
            .filter(adyenAuthorization -> adyenAuthorization.isCompleted())
            .firstOrError()
            .flatMap(adyenAuthorization -> createBundle())
            .observeOn(viewScheduler)
            .doOnSuccess(bundle -> {
              waitingResult = false;
              sendPaymentEvent();
              sendRevenueEvent();
              navigator.popView(bundle);
            })
            .doOnSuccess(__ -> view.showSuccess()))
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private Single<Bundle> createBundle() {
    return transactionBuilder.flatMap(transaction -> {
      if (type.equals("INAPP")) {
        return billing.getSkuPurchase(appPackage, skuId, Schedulers.io())
            .retryWhen(throwableFlowable -> throwableFlowable.delay(3, TimeUnit.SECONDS)
                .map(throwable -> 0)
                .timeout(3, TimeUnit.MINUTES))
            .map(purchase -> billingMessagesMapper.mapPurchase(purchase,
                transaction.getOrderReference()));
      } else {
        return inAppPurchaseInteractor.getTransactionUid(billingService.getTransactionUid())
            .retryWhen(errors -> {
              AtomicInteger counter = new AtomicInteger();
              return errors.takeWhile(e -> counter.getAndIncrement() != 3)
                  .flatMap(e -> Flowable.timer(counter.get(), TimeUnit.SECONDS));
            })
            .map(billingMessagesMapper::successBundle);
      }
    });
  }

  private void onViewCreatedCheckAuthorizationFailed() {
    disposables.add(transactionBuilder.flatMap(
        transaction -> billingService.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(currency), currency,
            type, transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage)
            .filter(payment -> payment.isFailed())
            .firstOrError()
            .observeOn(viewScheduler)
            .doOnSuccess(adyenAuthorization -> showError(adyenAuthorization)))
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void showError(AdyenAuthorization adyenAuthorization) {
    view.showPaymentRefusedError(adyenAuthorization);
  }

  private void onViewCreatedCheckAuthorizationProcessing() {
    disposables.add(transactionBuilder.flatMapObservable(
        transaction -> billingService.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(currency), currency,
            type, transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage)
            .filter(payment -> payment.isProcessing())
            .observeOn(viewScheduler)
            .doOnNext(__ -> view.showLoading()))
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void handlePaymentMethodResults() {
    disposables.add(view.paymentMethodDetailsEvent()
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(adyen::finishPayment)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void handleChangeCardMethodResults() {
    disposables.add(view.changeCardMethodDetailsEvent()
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(adyen::deletePaymentMethod)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void handleAdyenUriResult() {
    disposables.add(navigator.uriResults()
        .flatMapCompletable(uri -> adyen.finishUri(uri))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void handleAdyenUriRedirect() {
    disposables.add(adyen.getRedirectUrl()
        .observeOn(viewScheduler)
        .filter(s -> !waitingResult)
        .flatMapSingle(redirectUrl -> transactionBuilder.doOnSuccess(transaction -> {
          view.showLoading();
          navigator.navigateToUriForResult(redirectUrl, billingService.getTransactionUid(),
              transaction.getDomain(), transaction.getSkuId(), transaction.amount(),
              transaction.getType());
          waitingResult = true;
        }))
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
        //.doOnNext(__ -> popViewWithError())
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        }));
  }

  private void handleAdyenPaymentResult() {
    disposables.add(adyen.getPaymentResult()
        .flatMapCompletable(result -> {
          if (result.isProcessed()) {
            return billingService.authorize(result.getPayment(), result.getPayment()
                .getPayload());
          }
          return Completable.error(result.getError());
        })
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void handleCancel() {
    disposables.add(view.cancelEvent()
        .observeOn(viewScheduler)
        .doOnNext(__ -> {
          //analytics.sendAuthorizationCancelEvent(serviceName);
          //navigator.popView();
          close();
        })
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void close() {
    view.close(billingMessagesMapper.mapCancellation());
  }

  public void stop() {
    disposables.clear();
  }

  public void sendPaymentMethodDetailsEvent() {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPaymentMethodDetailsEvent(appPackage,
            transactionBuilder.getSkuId(), transactionBuilder.amount()
                .toString(), PAYMENT_METHOD_CC, transactionBuilder.getType())));
  }

  public void sendPaymentEvent() {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPaymentEvent(appPackage, transactionBuilder.getSkuId(),
            transactionBuilder.amount()
                .toString(), PAYMENT_METHOD_CC, transactionBuilder.getType())));
  }

  public void sendRevenueEvent() {
    disposables.add(transactionBuilder.subscribe(transactionBuilder -> analytics.sendRevenueEvent(
        inAppPurchaseInteractor.convertToFiat(transactionBuilder.amount()
            .doubleValue(), EVENT_REVENUE_CURRENCY)
            .blockingGet()
            .getAmount()
            .setScale(2, BigDecimal.ROUND_UP)
            .toString())));
  }

  void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(WAITING_RESULT, waitingResult);
  }
}