package com.asfoundation.wallet.ui.iab;

import android.util.Log;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabPresenter {
  private static final String TAG = IabPresenter.class.getSimpleName();
  private final IabView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
  }

  public void present(String uriString, String appPackage, String productName) {
    setupUi(uriString);

    handleCancelClick();

    handleOkErrorClick(uriString);

    handleBuyEvent(appPackage, productName);

    showTransactionState(uriString);

    handleDontShowMicroRaidenInfo();

    showChannelAmount();

    showMicroRaidenInfo();
  }

  private void showChannelAmount() {
    disposables.add(view.getCreateChannelClick()
        .doOnNext(isChecked -> {
          if (isChecked) {
            view.showChannelAmount();
          } else {
            view.hideChannelAmount();
          }
        })
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe());
  }

  private void handleDontShowMicroRaidenInfo() {
    disposables.add(view.getDontShowAgainClick()
        .doOnNext(__ -> inAppPurchaseInteractor.dontShowAgain())
        .subscribe());
  }

  private void showMicroRaidenInfo() {
    disposables.add(view.getCreateChannelClick()
        .filter(isChecked -> isChecked && inAppPurchaseInteractor.shouldShowDialog())
        .subscribe(__ -> view.showRaidenInfo()));
  }

  private void showTransactionState(String uriString) {
    disposables.add(inAppPurchaseInteractor.getTransactionState(uriString)
        .observeOn(viewScheduler)
        .flatMapCompletable(this::showPendingTransaction)
        .subscribe(() -> {
        }, throwable -> throwable.printStackTrace()));
  }

  private boolean handleBuyEvent(String appPackage, String productName) {
    return disposables.add(view.getBuyClick()
        .flatMapCompletable(uri -> inAppPurchaseInteractor.send(uri, appPackage, productName)
            .observeOn(viewScheduler)
            .doOnError(this::showError))
        .retry()
        .subscribe());
  }

  private void handleOkErrorClick(String uriString) {
    disposables.add(view.getOkErrorClick()
        .flatMapSingle(__ -> inAppPurchaseInteractor.parseTransaction(uriString))
        .subscribe(click -> showBuy(), throwable -> close()));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void setupUi(String uriString) {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uriString)
        .observeOn(viewScheduler)
        .subscribe(this::setup, this::showError));
  }

  private void showBuy() {
    view.showBuy();
  }

  private void close() {
    view.close();
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

  private Completable showPendingTransaction(PaymentTransaction transaction) {
    Log.d(TAG, "present: " + transaction);
    switch (transaction.getState()) {
      case COMPLETED:
        return Completable.fromAction(view::showTransactionCompleted)
            .andThen(Completable.timer(1, TimeUnit.SECONDS))
            .andThen(Completable.fromAction(() -> {
              view.finish(transaction.getBuyHash());
            }))
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case NO_FUNDS:
        return Completable.fromAction(() -> view.showNoFundsError())
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
      case WRONG_NETWORK:
      case UNKNOWN_TOKEN:
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
      case PENDING:
      case APPROVING:
      case APPROVED:
        return Completable.fromAction(view::showApproving);
      case BUYING:
      case BOUGHT:
        return Completable.fromAction(view::showBuying);
      default:
      case ERROR:
        return Completable.fromAction(() -> showError(null))
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
    }
  }

  public void stop() {
    disposables.clear();
  }

  private void setup(TransactionBuilder transaction) {
    view.setup(transaction);
    view.showRaidenChannelValues(
        inAppPurchaseInteractor.getTopUpChannelSuggestionValues(transaction.amount()));
  }
}
