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
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY;
import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_CC;
import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_PAYPAL;

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
  private final String developerPayload;
  private final Billing billing;
  private final String skuId;
  private final String type;
  private final String origin;
  private final String amount;
  private final String currency;
  private final String appPackage;
  private final PaymentType paymentType;
  private final Single<TransactionBuilder> transactionBuilder;
  private AdyenAuthorizationView view;
  private BillingAnalytics analytics;
  private boolean waitingResult;
  private Scheduler ioScheduler;
  private boolean isPreSelected;

  AdyenAuthorizationPresenter(AdyenAuthorizationView view, String appPackage,
      Scheduler viewScheduler, CompositeDisposable disposables, Adyen adyen,
      BillingService billingService, Navigator navigator,
      BillingMessagesMapper billingMessagesMapper, InAppPurchaseInteractor inAppPurchaseInteractor,
      String transactionData, String developerPayload, Billing billing, String skuId, String type,
      String origin, String amount, String currency, PaymentType paymentType,
      BillingAnalytics analytics, Scheduler ioScheduler, boolean isPreSelected) {
    this.view = view;
    this.appPackage = appPackage;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.adyen = adyen;
    this.billingService = billingService;
    this.navigator = navigator;
    this.billingMessagesMapper = billingMessagesMapper;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
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
    this.isPreSelected = isPreSelected;
  }

  public void present(@Nullable Bundle savedInstanceState) {
    adyen.createNewPayment();

    if (savedInstanceState != null) {
      waitingResult = savedInstanceState.getBoolean(WAITING_RESULT);
    }

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

    handleMorePaymentMethodClicks();

    handleFieldValidationStateChange();
  }

  private void handleMorePaymentMethodClicks() {
    if (isPreSelected) {
      disposables.add(view.getMorePaymentMethodsClicks()
          .observeOn(viewScheduler)
          .doOnNext(o -> showMoreMethods())
          .subscribe(__ -> {
          }, this::showError));
    }
  }

  private void onViewCreatedShowPaymentMethodInputView() {
    disposables.add(adyen.getPaymentRequest()
        .filter(paymentRequest -> paymentRequest.getPaymentMethod() != null)
        .map(paymentRequest -> paymentRequest.getPaymentMethod()
            .getType())
        .distinctUntilChanged(String::equals)
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
        }, this::showError));
  }

  private void showError(Throwable throwable) {
    throwable.printStackTrace();
    if (isNoNetworkException(throwable)) {
      view.showNetworkError();
    } else {
      view.showGenericError();
    }
  }

  private boolean isNoNetworkException(Throwable throwable) {
    return (throwable instanceof IOException) || (throwable.getCause() != null
        && throwable.getCause() instanceof IOException);
  }

  private void onViewCreatedCompletePayment() {
    disposables.add(Completable.fromAction(() -> view.showLoading())
        .andThen(transactionBuilder.flatMapCompletable(
            transaction -> billingService.getAuthorization(transaction.getSkuId(),
                transaction.toAddress(), developerPayload, origin, convertAmount(), currency, type,
                transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage,
                transaction.getUrl(), transaction.getUrlSignature())
                .observeOn(viewScheduler)
                .filter(AdyenAuthorization::isPendingAuthorization)
                .firstOrError()
                .map(payment -> payment)
                .flatMapCompletable(
                    authorization -> adyen.completePayment(authorization.getSession()))
                .observeOn(viewScheduler)))
        .observeOn(viewScheduler)
        .doOnError(this::showError)
        .subscribe(() -> {
        }, this::showError));
  }

  @NonNull private BigDecimal convertAmount() {
    return inAppPurchaseInteractor.convertToLocalFiat((new BigDecimal(amount)).doubleValue())
        .subscribeOn(ioScheduler)
        .blockingGet()
        .getAmount();
  }

  private void onViewCreatedSelectPaymentMethod() {
    disposables.add(adyen.getPaymentMethod(paymentType)
        .flatMapCompletable(adyen::selectPaymentService)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, this::showError));
  }

  private void onViewCreatedCheckAuthorizationActive() {
    disposables.add(transactionBuilder.flatMapCompletable(
        transaction -> billingService.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(), currency, type,
            transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage,
            transaction.getUrl(), transaction.getUrlSignature())
            .filter(AdyenAuthorization::isCompleted)
            .firstOrError()
            .flatMap(adyenAuthorization -> createBundle())
            .observeOn(viewScheduler)
            .flatMapCompletable(bundle -> Completable.fromAction(() -> {
              waitingResult = false;
              sendPaymentEvent(paymentType);
              sendRevenueEvent();
              view.showSuccess();
            })
                .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
                .andThen(Completable.fromAction(() -> navigator.popView(bundle)))))
        .observeOn(viewScheduler)
        .doOnError(this::showError)
        .subscribe(() -> {
        }, this::showError));
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
    })
        .map(bundle -> mapPaymentMethodId(bundle, paymentType));
  }

  private Bundle mapPaymentMethodId(Bundle bundle, PaymentType paymentType) {
    if (paymentType.name()
        .equals("CARD")) {
      bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.CREDIT_CARD.getId());
    } else if (paymentType.name()
        .equals("PAYPAL")) {
      bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.PAYPAL.getId());
    }
    return bundle;
  }

  private void onViewCreatedCheckAuthorizationFailed() {
    disposables.add(transactionBuilder.flatMap(
        transaction -> billingService.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(), currency, type,
            transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage,
            transaction.getUrl(), transaction.getUrlSignature())
            .filter(AdyenAuthorization::isFailed)
            .firstOrError()
            .observeOn(viewScheduler)
            .doOnSuccess(this::showError))
        .observeOn(viewScheduler)
        .doOnError(this::showError)
        .subscribe(__ -> {
        }, this::showError));
  }

  private void showError(AdyenAuthorization adyenAuthorization) {
    view.showPaymentRefusedError(adyenAuthorization);
  }

  private void onViewCreatedCheckAuthorizationProcessing() {
    disposables.add(transactionBuilder.flatMapObservable(
        transaction -> billingService.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(), currency, type,
            transaction.getCallbackUrl(), transaction.getOrderReference(), appPackage,
            transaction.getUrl(), transaction.getUrlSignature())
            .filter(AdyenAuthorization::isProcessing)
            .observeOn(viewScheduler)
            .doOnNext(__ -> view.showLoading()))
        .observeOn(viewScheduler)
        .doOnError(this::showError)
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handlePaymentMethodResults() {
    disposables.add(view.paymentMethodDetailsEvent()
        .doOnNext(__ -> {
          view.showLoading();
          view.lockRotation();
        })
        .flatMapCompletable(adyen::finishPayment)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleChangeCardMethodResults() {
    if (!isPreSelected) {
      disposables.add(view.changeCardMethodDetailsEvent()
          .doOnNext(__ -> view.showLoading())
          .flatMapCompletable(paymentMethod -> adyen.deletePaymentMethod())
          .observeOn(viewScheduler)
          .subscribe(() -> {
          }, this::showError));
    }
  }

  private void handleAdyenUriResult() {
    disposables.add(navigator.uriResults()
        .flatMapCompletable(adyen::finishUri)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleAdyenUriRedirect() {
    disposables.add(adyen.getRedirectUrl()
        .observeOn(viewScheduler)
        .filter(s -> !waitingResult)
        .flatMapSingle(redirectUrl -> transactionBuilder.doOnSuccess(transaction -> {
          view.showLoading();
          view.lockRotation();
          navigator.navigateToUriForResult(redirectUrl);
          waitingResult = true;
          sendPaymentMethodDetailsEvent(PAYMENT_METHOD_PAYPAL);
        }))
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
        .doOnNext(__ -> navigator.popViewWithError())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
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
        }, this::showError));
  }

  private void handleCancel() {
    disposables.add(view.cancelEvent()
        .observeOn(viewScheduler)
        .doOnNext(__ -> close())
        .subscribe(__ -> {
        }, this::showError));
  }

  private void close() {
    view.close(billingMessagesMapper.mapCancellation());
  }

  private void showMoreMethods() {
    inAppPurchaseInteractor.removePreSelectedPaymentMethod();
    view.showMoreMethods();
  }

  public void stop() {
    disposables.clear();
  }

  void sendPaymentMethodDetailsEvent(String paymentMethod) {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPaymentMethodDetailsEvent(appPackage,
            transactionBuilder.getSkuId(), transactionBuilder.amount()
                .toString(), paymentMethod, transactionBuilder.getType())));
  }

  private void sendPaymentEvent(PaymentType paymentType) {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPaymentEvent(appPackage, transactionBuilder.getSkuId(),
            transactionBuilder.amount()
                .toString(), mapPayment(paymentType), transactionBuilder.getType())));
  }

  private void sendRevenueEvent() {
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

  private String mapPayment(PaymentType paymentType) {
    if (paymentType == PaymentType.CARD) {
      return PAYMENT_METHOD_CC;
    } else {
      return PAYMENT_METHOD_PAYPAL;
    }
  }

  private void handleFieldValidationStateChange() {
    disposables.add(view.onValidFieldStateChange()
        .observeOn(viewScheduler)
        .doOnNext(valid -> view.updateButton(valid))
        .subscribe());
  }
}