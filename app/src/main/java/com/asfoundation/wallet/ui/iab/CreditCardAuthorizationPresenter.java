package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.adyen.core.models.PaymentMethod;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.asfoundation.wallet.billing.CreditCardBilling;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.payment.Adyen;
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

import static com.asfoundation.wallet.billing.analytics.BillingAnalytics.PAYMENT_METHOD_CC;
import static com.asfoundation.wallet.ui.iab.ExpressCheckoutBuyFragment.serializeJson;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public class CreditCardAuthorizationPresenter {

  private static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
  private static final String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";
  private static final String INAPP_PURCHASE_ID = "INAPP_PURCHASE_ID";

  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final Adyen adyen;
  private final CreditCardBilling creditCardBilling;
  private final CreditCardNavigator navigator;
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
  private CreditCardAuthorizationView view;
  private FindDefaultWalletInteract defaultWalletInteract;
  private BillingAnalytics analytics;

  public CreditCardAuthorizationPresenter(CreditCardAuthorizationView view, String appPackage,
      FindDefaultWalletInteract defaultWalletInteract, Scheduler viewScheduler,
      CompositeDisposable disposables, Adyen adyen, CreditCardBilling creditCardBilling,
      CreditCardNavigator navigator, BillingMessagesMapper billingMessagesMapper,
      InAppPurchaseInteractor inAppPurchaseInteractor, String transactionData,
      String developerPayload, Billing billing, String skuId, String type, String origin,
      String amount, String currency, BillingAnalytics analytics) {
    this.view = view;
    this.appPackage = appPackage;
    this.defaultWalletInteract = defaultWalletInteract;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.adyen = adyen;
    this.creditCardBilling = creditCardBilling;
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
    this.analytics = analytics;
  }

  public void present() {
    disposables.add(defaultWalletInteract.find()
        .doOnSuccess(wallet -> adyen.createToken())
        .observeOn(viewScheduler)
        .doOnSuccess(wallet -> view.showWalletAddress(wallet.address))
        .subscribe(wallet -> {
        }, this::showError));

    handleCancelClick();

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

  private void onViewCreatedShowCreditCardInputView() {
    disposables.add(adyen.getPaymentData()
        .observeOn(viewScheduler)
        .doOnSuccess(data -> {
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
      view.showNetworkError();
    }
  }

  private void onViewCreatedCreatePayment() {
    disposables.add(Completable.fromAction(() -> view.showLoading())
        .andThen(inAppPurchaseInteractor.parseTransaction(transactionData, true)
            .flatMapCompletable(
                transaction -> creditCardBilling.getAuthorization(transaction.getSkuId(),
                    transaction.toAddress(), developerPayload, origin, convertAmount(currency),
                    currency, type)
                    .observeOn(viewScheduler)
                    .filter(payment -> payment.isPendingAuthorization())
                    .firstOrError()
                    .map(payment -> payment)
                    .flatMapCompletable(
                        authorization -> adyen.createPayment(authorization.getSession()))
                    .observeOn(viewScheduler)))
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  @NonNull private BigDecimal convertAmount(String currency) {
    return BigDecimal.valueOf(
        inAppPurchaseInteractor.convertToFiat((new BigDecimal(amount)).doubleValue(), currency)
            .blockingGet()
            .getAmount())
        .setScale(2, BigDecimal.ROUND_UP);
  }

  private void onViewCreatedSelectCreditCardPayment() {
    disposables.add(adyen.getCreditCardPaymentService()
        .flatMapCompletable(creditCard -> adyen.selectPaymentService(creditCard))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
  }

  private void onViewCreatedCheckAuthorizationActive() {
    disposables.add(inAppPurchaseInteractor.parseTransaction(transactionData, true)
        .flatMap(transaction -> creditCardBilling.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(currency), currency,
            type)
            .filter(payment -> payment.isCompleted())
            .firstOrError()
            .flatMap(adyenAuthorization -> buildBundle(billing))
            .observeOn(viewScheduler)
            .doOnSuccess(bundle -> {
              sendPaymentEvent(PAYMENT_METHOD_CC);
              navigator.popView(bundle);
            })
            .doOnSuccess(__ -> view.showSuccess()))
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private Single<Bundle> buildBundle(Billing billing) {
    return Single.just(new Bundle())
        .flatMap(bundle -> {
          if (type.equals("INAPP")) {
            return billing.getSkuPurchase(appPackage, skuId, Schedulers.io())
                .retryWhen(throwableFlowable -> throwableFlowable.delay(3, TimeUnit.SECONDS)
                    .map(throwable -> 0)
                    .timeout(3, TimeUnit.MINUTES))
                .doOnSuccess(purchase -> {
                  bundle.putString(INAPP_PURCHASE_DATA, serializeJson(purchase));
                  bundle.putString(INAPP_DATA_SIGNATURE, purchase.getSignature()
                      .getValue());
                  bundle.putString(INAPP_PURCHASE_ID, purchase.getUid());
                })
                .map(purchase -> bundle);
          } else {
            return inAppPurchaseInteractor.getTransactionUid(creditCardBilling.getTransactionUid())
                .retryWhen(errors -> {
                  AtomicInteger counter = new AtomicInteger();
                  return errors.takeWhile(e -> counter.getAndIncrement() != 3)
                      .flatMap(e -> Flowable.timer(counter.get(), TimeUnit.SECONDS));
                })
                .doOnSuccess(txHash -> bundle.putString(IabActivity.TRANSACTION_HASH, txHash))
                .map(s -> bundle);
          }
        });
  }

  private void onViewCreatedCheckAuthorizationFailed() {
    disposables.add(inAppPurchaseInteractor.parseTransaction(transactionData, true)
        .flatMap(transaction -> creditCardBilling.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(currency), currency,
            type)
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
    disposables.add(inAppPurchaseInteractor.parseTransaction(transactionData, true)
        .flatMapObservable(transaction -> creditCardBilling.getAuthorization(transaction.getSkuId(),
            transaction.toAddress(), developerPayload, origin, convertAmount(currency), currency,
            type)
            .filter(payment -> payment.isProcessing())
            .observeOn(viewScheduler)
            .doOnNext(__ -> view.showLoading()))
        .subscribe(__ -> {
        }, throwable -> showError(throwable)));
  }

  private void handleAdyenCreditCardResults() {
    disposables.add(view.creditCardDetailsEvent()
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(details -> adyen.finishPayment(details))
        .observeOn(viewScheduler)
        .subscribe(() -> {
        }, throwable -> showError(throwable)));
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
    disposables.add(adyen.getRedirectUrl()
        .observeOn(viewScheduler)
        .doOnSuccess(redirectUrl -> {
          view.showLoading();
          //navigator.navigateToUriForResult(redirectUrl);
        })
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
            return creditCardBilling.authorize(result.getPayment(), result.getPayment()
                .getPayload());
            //return billing.authorize(sku, result.getAuthorization()
            //    .getPayload());
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

  private void handleCancelClick() {
    disposables.add(view.cancelEvent()
        .doOnNext(click -> close())
        .subscribe());
  }

  private void close() {
    view.close(billingMessagesMapper.mapCancellation());
  }

  public void stop() {
    disposables.clear();
  }

  public void sendCCDetailsEvent() {
    analytics.sendCreditCardDetailsEvent(appPackage, skuId, amount);
  }

  public void sendPaymentEvent(String purchaseDetails) {
    analytics.sendPaymentEvent(appPackage, skuId, amount, purchaseDetails);
  }
}