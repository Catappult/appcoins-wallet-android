package com.asfoundation.wallet.topup.payment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adyen.core.models.PaymentMethod;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asfoundation.wallet.billing.BillingService;
import com.asfoundation.wallet.billing.adyen.Adyen;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.Navigator;
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

public class PaymentAuthPresenter {

  private static final String WAITING_RESULT = "WAITING_RESULT";

  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final Adyen adyen;
  private final BillingService billingService;
  private final Navigator navigator;
  private final BillingMessagesMapper billingMessagesMapper;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final String appPackage;
  private PaymentAuthView view;
  private boolean waitingResult;

  public PaymentAuthPresenter(PaymentAuthView view, String appPackage, Scheduler viewScheduler,
      CompositeDisposable disposables, Adyen adyen, BillingService billingService,
      Navigator navigator, BillingMessagesMapper billingMessagesMapper,
      InAppPurchaseInteractor inAppPurchaseInteractor) {
    this.view = view;
    this.appPackage = appPackage;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.adyen = adyen;
    this.billingService = billingService;
    this.navigator = navigator;
    this.billingMessagesMapper = billingMessagesMapper;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
  }

  public void present(@Nullable Bundle savedInstanceState, String transactionOrigin, String amount,
      String currency, String transactionType, PaymentType paymentType) {
    adyen.createNewPayment();

    if (savedInstanceState != null) {
      waitingResult = savedInstanceState.getBoolean(WAITING_RESULT);
    }

    onViewCreatedCompletePayment(transactionOrigin, amount, currency, transactionType);

    onViewCreatedSelectPaymentMethod(paymentType);

    onViewCreatedShowPaymentMethodInputView();

    onViewCreatedCheckAuthorizationActive(transactionOrigin, amount, currency, transactionType);

    onViewCreatedCheckAuthorizationFailed(transactionOrigin, amount, currency, transactionType);

    onViewCreatedCheckAuthorizationProcessing(transactionOrigin, amount, currency, transactionType);

    handlePaymentMethodResults();

    handleChangeCardMethodResults();

    handleAdyenUriRedirect(appPackage, amount, transactionType);

    handleAdyenUriResult();

    handleErrorDismissEvent();

    handleAdyenPaymentResult();

    handleFieldValidationStateChange();
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
            view.showCreditCardView(data.getPaymentMethod(), true,
                data.getShopperReference() != null, data.getPublicKey(), data.getGenerationTime());
          } else {
            view.showCvcView(data.getPaymentMethod());
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

  private void onViewCreatedCompletePayment(String transactionOrigin, String amount,
      String currency, String transactionType) {
    disposables.add(Completable.fromAction(() -> view.showLoading())
        .andThen(convertAmount(amount).flatMapCompletable(
            value -> billingService.getAuthorization(transactionOrigin, value, currency,
                transactionType, appPackage)
                .observeOn(viewScheduler)
                .filter(payment -> payment.isPendingAuthorization())
                .firstOrError()
                .flatMapCompletable(
                    authorization -> adyen.completePayment(authorization.getSession()))))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  @NonNull private Single<BigDecimal> convertAmount(String amount) {
    return inAppPurchaseInteractor.convertToLocalFiat((new BigDecimal(amount)).doubleValue())
        .subscribeOn(Schedulers.io())
        .map(value -> value.getAmount()
            .setScale(2, BigDecimal.ROUND_UP));
  }

  private void onViewCreatedSelectPaymentMethod(PaymentType paymentType) {
    disposables.add(adyen.getPaymentMethod(paymentType)
        .flatMapCompletable(paymentMethod -> adyen.selectPaymentService(paymentMethod))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void onViewCreatedCheckAuthorizationActive(String transactionOrigin, String amount,
      String currency, String transactionType) {
    disposables.add(convertAmount(amount).flatMap(
        value -> billingService.getAuthorization(transactionOrigin, value, currency,
            transactionType, appPackage)
            .filter(adyenAuthorization -> adyenAuthorization.isCompleted())
            .firstOrError()
            .flatMap(adyenAuthorization -> createBundle())
            .observeOn(viewScheduler)
            .doOnSuccess(bundle -> {
              waitingResult = false;
              navigator.popView(bundle);
            }))
        .observeOn(viewScheduler)
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private Single<Bundle> createBundle() {
    return inAppPurchaseInteractor.getTransactionAmount(billingService.getTransactionUid())
        .retryWhen(errors -> {
          AtomicInteger counter = new AtomicInteger();
          return errors.takeWhile(e -> counter.getAndIncrement() != 3)
              .flatMap(e -> Flowable.timer(counter.get(), TimeUnit.SECONDS));
        })
        .map(billingMessagesMapper::topUpBundle);
  }

  private void onViewCreatedCheckAuthorizationFailed(String transactionOrigin, String amount,
      String currency, String transactionType) {
    disposables.add(convertAmount(amount).flatMap(
        value -> billingService.getAuthorization(transactionOrigin, value, currency,
            transactionType, appPackage)
            .filter(AdyenAuthorization::isFailed)
            .firstOrError()
            .observeOn(viewScheduler)
            .doOnSuccess(adyenAuthorization -> showError(adyenAuthorization)))
        .observeOn(viewScheduler)
        .subscribe(__ -> {
        }, this::showError));
  }

  private void showError(AdyenAuthorization adyenAuthorization) {
    view.showPaymentRefusedError(adyenAuthorization);
  }

  private void onViewCreatedCheckAuthorizationProcessing(String transactionOrigin, String amount,
      String currency, String transactionType) {
    disposables.add(convertAmount(amount).map(
        value -> billingService.getAuthorization(transactionOrigin, value, currency,
            transactionType, appPackage)
            .filter(AdyenAuthorization::isProcessing)
            .observeOn(viewScheduler)
            .doOnNext(__ -> view.showLoading()))
        .observeOn(viewScheduler)
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handlePaymentMethodResults() {
    disposables.add(view.paymentMethodDetailsEvent()
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(adyen::finishPayment)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleChangeCardMethodResults() {
    disposables.add(view.changeCardMethodDetailsEvent()
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(adyen::deletePaymentMethod)
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleAdyenUriResult() {
    disposables.add(navigator.uriResults()
        .flatMapCompletable(uri -> adyen.finishUri(uri))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, this::showError));
  }

  private void handleAdyenUriRedirect(String domain, String amount, String transactionType) {
    disposables.add(adyen.getRedirectUrl()
        .observeOn(viewScheduler)
        .filter(s -> !waitingResult)
        .doOnSuccess(redirectUrl -> {
          view.showLoading();
          navigator.navigateToUriForResult(redirectUrl, billingService.getTransactionUid(), domain,
              "", new BigDecimal(amount), transactionType);
          waitingResult = true;
        })
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
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

  private void handleFieldValidationStateChange() {
    disposables.add(view.onValidFieldStateChange()
        .observeOn(viewScheduler)
        .doOnNext(valid -> view.updateTopUpButton(valid))
        .subscribe());
  }

  public void stop() {
    disposables.clear();
  }

  void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(WAITING_RESULT, waitingResult);
  }
}