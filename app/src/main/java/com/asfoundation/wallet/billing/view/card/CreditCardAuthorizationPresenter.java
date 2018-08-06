package com.asfoundation.wallet.billing.view.card;

import com.adyen.core.models.PaymentMethod;
import com.asfoundation.wallet.billing.AdyenBilling;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.asfoundation.wallet.presenter.Presenter;
import com.asfoundation.wallet.presenter.View;
import java.io.IOException;
import rx.Completable;
import rx.Scheduler;
import rx.exceptions.OnErrorNotImplementedException;

@Deprecated
public class CreditCardAuthorizationPresenter implements Presenter {

  private final Adyen adyen;
  private final CreditCardAuthorizationView view;
  private final Scheduler viewScheduler;
  private final AdyenBilling adyenBilling;
  private final CreditCardNavigator navigator;

  public CreditCardAuthorizationPresenter(Adyen adyen, CreditCardAuthorizationView view,
      Scheduler viewScheduler, AdyenBilling adyenBilling, CreditCardNavigator navigator) {
    this.adyen = adyen;
    this.view = view;
    this.viewScheduler = viewScheduler;
    this.adyenBilling = adyenBilling;
    this.navigator = navigator;
  }

  private void onViewCreatedShowCreditCardInputView() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> adyen.getPaymentData())
        .observeOn(viewScheduler)
        .doOnNext(data -> {
          view.hideLoading();
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
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void showError(Throwable throwable) {
    if (throwable instanceof IOException) {
      view.hideLoading();
      view.showNetworkError();
    } else {
      view.showNetworkError();
    }
  }

  @Override public void present() {

    onViewCreatedCreatePayment();

    onViewCreatedSelectCreditCardPayment();

    onViewCreatedShowCreditCardInputView();

    onViewCreatedCheckAuthorizationActive();

    onViewCreatedCheckAuthorizationFailed();

    onViewCreatedCheckAuthorizationProcessing();

    handleAdyenCreditCardResults();

    handleAdyenUriRedirect();

    //handleAdyenUriResult();

    handleErrorDismissEvent();

    handleAdyenPaymentResult();

    handleCancel();
  }

  private void onViewCreatedCreatePayment() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .doOnNext(__ -> view.showLoading())
        .flatMap(__ -> adyenBilling.getAuthorization())
        .observeOn(viewScheduler)
        //.doOnNext(payment -> view.showProduct(payment.getProduct()))
        .first(payment -> payment.isPendingAuthorization())
        .map(payment -> payment)
        //.cast(AdyenAuthorization.class)
        .flatMapCompletable(authorization -> adyen.createPayment(authorization.getSession()))
        .observeOn(viewScheduler)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void onViewCreatedSelectCreditCardPayment() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> adyen.getCreditCardPaymentService())
        .flatMapCompletable(creditCard -> adyen.selectPaymentService(creditCard))
        .observeOn(viewScheduler)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void onViewCreatedCheckAuthorizationActive() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> adyenBilling.getAuthorization())
        .first(payment -> payment.isCompleted())
        //.doOnNext(payment -> analytics.sendAuthorizationSuccessEvent(payment))
        .observeOn(viewScheduler)
        .doOnNext(__ -> navigator.popView(adyenBilling.getTransactionUid()))
        .doOnNext(__ -> view.showSuccess())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void onViewCreatedCheckAuthorizationFailed() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> adyenBilling.getAuthorization())
        .first(payment -> payment.isFailed())
        .observeOn(viewScheduler)
        .doOnNext(__ -> navigator.popViewWithError())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void onViewCreatedCheckAuthorizationProcessing() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> adyenBilling.getAuthorization())
        .filter(payment -> payment.isProcessing())
        .observeOn(viewScheduler)
        .doOnNext(__ -> view.showLoading())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void handleAdyenCreditCardResults() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(authorization -> view.creditCardDetailsEvent())
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(details -> adyen.finishPayment(details))
        .observeOn(viewScheduler)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  //private void handleAdyenUriResult() {
  //  view.getLifecycleEvents()
  //      .filter(event -> event.equals(View.LifecycleEvent.CREATE))
  //      .flatMap(__ -> navigator.uriResults())
  //      .flatMapCompletable(uri -> adyen.finishUri(uri))
  //      .observeOn(viewScheduler)
  //      .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
  //      .subscribe(__ -> {
  //      }, throwable -> showError(throwable));
  //}

  private void handleAdyenUriRedirect() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> adyen.getRedirectUrl())
        .observeOn(viewScheduler)
        .doOnNext(redirectUrl -> {
          view.showLoading();
          //navigator.navigateToUriForResult(redirectUrl);
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void handleErrorDismissEvent() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.errorDismisses())
        //.doOnNext(__ -> popViewWithError())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private void handleAdyenPaymentResult() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMapSingle(__ -> adyen.getPaymentResult())
        .flatMapCompletable(result -> {
          if (result.isProcessed()) {
            return adyenBilling.authorize(result.getPayment(), result.getPayment()
                .getPayload());
            //return billing.authorize(sku, result.getAuthorization()
            //    .getPayload());
          }
          return Completable.error(result.getError());
        })
        .observeOn(viewScheduler)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }

  private void handleCancel() {
    view.getLifecycleEvents()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.cancelEvent())
        .observeOn(viewScheduler)
        .doOnNext(__ -> {
          //analytics.sendAuthorizationCancelEvent(serviceName);
          //navigator.popView();
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> showError(throwable));
  }
}
