package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import android.util.Log;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.repository.entity.TransactionData.TransactionType;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import static com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public class OnChainBuyPresenter {

  private static final String TAG = OnChainBuyPresenter.class.getSimpleName();
  private final OnChainBuyView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final BillingMessagesMapper billingMessagesMapper;
  private final boolean isBds;
  private final String productName;
  private final String appPackage;
  private final String uriString;
  private final Single<TransactionBuilder> transactionBuilder;
  private BillingAnalytics analytics;
  private Disposable statusDisposable;

  public OnChainBuyPresenter(OnChainBuyView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables,
      BillingMessagesMapper billingMessagesMapper, boolean isBds, String productName,
      BillingAnalytics analytics, String appPackage, String uriString) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.billingMessagesMapper = billingMessagesMapper;
    this.isBds = isBds;
    this.productName = productName;
    this.analytics = analytics;
    this.appPackage = appPackage;
    this.uriString = uriString;
    this.transactionBuilder = inAppPurchaseInteractor.parseTransaction(uriString, isBds);
  }

  public void present(String uriString, String appPackage, String productName, BigDecimal amount,
      String developerPayload) {
    setupUi(amount, uriString, appPackage, developerPayload);

    handleCancelClick();

    handleOkErrorClick(uriString);

    handleBuyEvent(appPackage, productName, developerPayload, isBds);
  }

  private void showTransactionState(String uriString) {
    if (statusDisposable != null && !statusDisposable.isDisposed()) {
      statusDisposable.dispose();
    }
    statusDisposable = inAppPurchaseInteractor.getTransactionState(uriString)
        .observeOn(viewScheduler)
        .flatMapCompletable(this::showPendingTransaction)
        .subscribe(() -> {
        }, this::showError);
  }

  private void handleBuyEvent(String appPackage, String productName, String developerPayload,
      boolean isBds) {
    disposables.add(view.getBuyClick()
        .doOnNext(this::showTransactionState)
        .observeOn(Schedulers.io())
        .flatMapCompletable(buyData -> inAppPurchaseInteractor.send(buyData,
            AsfInAppPurchaseInteractor.TransactionType.NORMAL, appPackage, productName,
            BigDecimal.ZERO, developerPayload, isBds)
            .observeOn(viewScheduler)
            .doOnError(this::showError))
        .retry()
        .subscribe());
  }

  private void handleOkErrorClick(String uriString) {
    disposables.add(view.getOkErrorClick()
        .flatMapSingle(__ -> inAppPurchaseInteractor.parseTransaction(uriString, isBds))
        .subscribe(click -> close(), throwable -> close()));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void setupUi(BigDecimal appcAmount, String uri, String packageName,
      String developerPayload) {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uri, isBds)
        .flatMapCompletable(
            transaction -> inAppPurchaseInteractor.getCurrentPaymentStep(packageName, transaction)
                .flatMapCompletable(currentPaymentStep -> {
                  switch (currentPaymentStep) {
                    case PAUSED_ON_CHAIN:
                      return inAppPurchaseInteractor.resume(uri,
                          AsfInAppPurchaseInteractor.TransactionType.NORMAL, packageName,
                          transaction.getSkuId(), developerPayload, isBds);
                    case READY:
                      return Completable.fromAction(() -> setup(appcAmount, transaction.getType()))
                          .subscribeOn(AndroidSchedulers.mainThread());
                    case NO_FUNDS:
                      return Completable.fromAction(view::showNoFundsError)
                          .subscribeOn(viewScheduler);
                    case PAUSED_CC_PAYMENT:
                    default:
                      return Completable.error(new UnsupportedOperationException(
                          "Cannot resume from " + currentPaymentStep.name() + " status"));
                  }
                }))
        .subscribe(() -> {
        }, this::showError));

    disposables.add(inAppPurchaseInteractor.getWalletAddress()
        .observeOn(viewScheduler)
        .subscribe(wallet -> view.showWallet(wallet), this::showError));
  }

  private void close() {
    view.close(billingMessagesMapper.mapCancellation());
  }

  private void showError(@Nullable Throwable throwable) {
    if (throwable != null) {
      throwable.printStackTrace();
    }
    if (throwable instanceof UnknownTokenException) {
      view.showWrongNetworkError();
    } else {
      view.showError();
    }
  }

  private Completable showPendingTransaction(Payment transaction) {
    Log.d(TAG, "present: " + transaction);
    switch (transaction.getStatus()) {
      case COMPLETED:
        return inAppPurchaseInteractor.getCompletedPurchase(transaction, isBds)
            .observeOn(AndroidSchedulers.mainThread())
            .map(payment -> buildBundle(payment, transaction.getOrderReference()))
            .flatMapCompletable(bundle -> Completable.fromAction(view::showTransactionCompleted)
                .subscribeOn(AndroidSchedulers.mainThread())
                .andThen(Completable.timer(1, TimeUnit.SECONDS))
                .andThen(Completable.fromRunnable(() -> view.finish(bundle))))
            .onErrorResumeNext(throwable -> Completable.fromAction(() -> showError(throwable)));
      case NO_FUNDS:
        return Completable.fromAction(() -> view.showNoFundsError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case NETWORK_ERROR:
        return Completable.fromAction(() -> view.showWrongNetworkError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case NO_TOKENS:
        return Completable.fromAction(() -> view.showNoTokenFundsError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case NO_ETHER:
        return Completable.fromAction(() -> view.showNoEtherFundsError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case NO_INTERNET:
        return Completable.fromAction(() -> view.showNoNetworkError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case NONCE_ERROR:
        return Completable.fromAction(() -> view.showNonceError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case APPROVING:
        return Completable.fromAction(view::showApproving);
      case BUYING:
        return Completable.fromAction(view::showBuying);
      default:
      case ERROR:
        return Completable.fromAction(() -> showError(null))
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
    }
  }

  private Bundle buildBundle(Payment payment, String orderReference) {
    if (payment.getUid() != null
        && payment.getSignature() != null
        && payment.getSignatureData() != null) {
      return billingMessagesMapper.mapPurchase(payment.getUid(), payment.getSignature(),
          payment.getSignatureData(), orderReference);
    } else {
      Bundle bundle = new Bundle();
      bundle.putInt(IabActivity.RESPONSE_CODE, 0);
      bundle.putString(IabActivity.TRANSACTION_HASH, payment.getBuyHash());

      return bundle;
    }
  }

  public void stop() {
    disposables.clear();
  }

  private void setup(BigDecimal amount, String type) {
    view.setup(productName, TransactionType.DONATION.name()
        .equalsIgnoreCase(type));
    view.showRaidenChannelValues(inAppPurchaseInteractor.getTopUpChannelSuggestionValues(amount));
  }

  public void sendPaymentEvent(String purchaseDetails) {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPaymentEvent(appPackage, transactionBuilder.getSkuId(),
            transactionBuilder.amount()
                .toString(), purchaseDetails, transactionBuilder.getType())));
  }

  public void resume() {
    showTransactionState(uriString);
  }

  public void pause() {
    statusDisposable.dispose();
  }

  public void sendRevenueEvent() {
    disposables.add(transactionBuilder.flatMap(
        transaction -> inAppPurchaseInteractor.convertToFiat((transaction.amount()).doubleValue(),
            EVENT_REVENUE_CURRENCY))
        .doOnSuccess(fiatValue -> analytics.sendRevenueEvent(String.valueOf(fiatValue.getAmount())))
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }
}
