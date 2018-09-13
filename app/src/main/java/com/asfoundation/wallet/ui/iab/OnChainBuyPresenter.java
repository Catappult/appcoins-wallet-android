package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import android.util.Log;
import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Created by franciscocalado on 19/07/2018.
 */

public class OnChainBuyPresenter {

  private static final String TAG = OnChainBuyPresenter.class.getSimpleName();
  private final OnChainBuyView view;
  private final BdsInAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final BillingMessagesMapper billingMessagesMapper;
  private final ExternalBillingSerializer billingSerializer;

  public OnChainBuyPresenter(OnChainBuyView view,
      BdsInAppPurchaseInteractor inAppPurchaseInteractor, Scheduler viewScheduler,
      CompositeDisposable disposables, BillingMessagesMapper billingMessagesMapper,
      ExternalBillingSerializer billingSerializer) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.billingMessagesMapper = billingMessagesMapper;
    this.billingSerializer = billingSerializer;
  }

  public void present(String uriString, String appPackage, String productName, BigDecimal amount,
      String developerPayload) {
    setupUi(amount, uriString, appPackage, developerPayload);

    handleCancelClick();

    handleOkErrorClick(uriString);

    handleBuyEvent(appPackage, productName, developerPayload);

    showTransactionState(uriString);

    handleDontShowMicroRaidenInfo();

    showChannelAmount();

    showMicroRaidenInfo();
  }

  private void showChannelAmount() {
    disposables.add(view.getCreateChannelClick()
        .flatMapSingle(isChecked -> inAppPurchaseInteractor.hasChannel()
            .observeOn(viewScheduler)
            .doOnSuccess(hasChannel -> {
              if (isChecked && !hasChannel) {
                view.showChannelAmount();
              } else {
                view.hideChannelAmount();
              }
            }))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe());
  }

  private void handleDontShowMicroRaidenInfo() {
    //disposables.add(view.getDontShowAgainClick()
    //    .doOnNext(__ -> inAppPurchaseInteractor.dontShowAgain())
    //    .subscribe());
  }

  private void showMicroRaidenInfo() {
    disposables.add(view.getCreateChannelClick()
        .filter(isChecked -> isChecked && inAppPurchaseInteractor.shouldShowDialog())
        .doOnNext(__ -> view.showRaidenInfo())
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe());
  }

  private void showTransactionState(String uriString) {
    disposables.add(inAppPurchaseInteractor.getTransactionState(uriString)
        .observeOn(viewScheduler)
        .flatMapCompletable(this::showPendingTransaction)
        .subscribe(() -> {
        }, throwable -> throwable.printStackTrace()));
  }

  private void handleBuyEvent(String appPackage, String productName, String developerPayload) {
    disposables.add(view.getBuyClick()
        .observeOn(Schedulers.io())
        .flatMapCompletable(buyData -> inAppPurchaseInteractor.send(buyData.getUri(),
            buyData.isRaiden ? InAppPurchaseInteractor.TransactionType.RAIDEN
                : InAppPurchaseInteractor.TransactionType.NORMAL, appPackage, productName,
            buyData.getChannelBudget(), developerPayload)
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

  private void setupUi(BigDecimal appcAmount, String uri, String packageName,
      String developerPayload) {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uri)
        .flatMapCompletable(
            transaction -> inAppPurchaseInteractor.getCurrentPaymentStep(packageName, transaction)
                .flatMapCompletable(currentPaymentStep -> {
                  switch (currentPaymentStep) {
                    case PAUSED_ON_CHAIN:
                      return inAppPurchaseInteractor.resume(uri,
                          InAppPurchaseInteractor.TransactionType.NORMAL, packageName,
                          transaction.getSkuId(), developerPayload);
                    case READY:
                      return Completable.fromAction(() -> setup(appcAmount))
                          .subscribeOn(AndroidSchedulers.mainThread());
                    case PAUSED_OFF_CHAIN:
                    case NO_FUNDS:
                    default:
                      return Completable.error(new UnsupportedOperationException(
                          "Cannot resume from " + currentPaymentStep.name() + " status"));
                  }
                }))
        .subscribe());

    disposables.add(inAppPurchaseInteractor.getWalletAddress()
        .observeOn(viewScheduler)
        .subscribe(view::showWallet, Throwable::printStackTrace));

    disposables.add(inAppPurchaseInteractor.getWalletAddress()
        .flatMap(wallet -> inAppPurchaseInteractor.hasChannel())
        .observeOn(viewScheduler)
        .subscribe(hasChannel -> {
          if (hasChannel) {
            view.showChannelAsDefaultPayment();
          } else {
            view.showDefaultAsDefaultPayment();
          }
        }, Throwable::printStackTrace));
  }

  private void showBuy() {
    view.showBuy();
  }

  private void close() {
    view.close(billingMessagesMapper.mapCancellation());
  }

  private void showError(@Nullable Throwable throwable) {
    if (throwable != null) {
      throwable.printStackTrace();
    }
    if (throwable instanceof NotEnoughFundsException) {
      view.showNoChannelFundsError();
    } else if (throwable instanceof UnknownTokenException) {
      view.showWrongNetworkError();
    } else {
      view.showError();
    }
  }

  private Completable showPendingTransaction(Payment transaction) {
    Log.d(TAG, "present: " + transaction);
    switch (transaction.getStatus()) {
      case COMPLETED:
        return Completable.fromAction(view::showTransactionCompleted)
            .andThen(Completable.timer(1, TimeUnit.SECONDS))
            .observeOn(Schedulers.io())
            .andThen(Completable.fromAction(() -> {
              Bundle bundle = new Bundle();
              bundle.putInt("RESPONSE_CODE", 0);
              bundle.putString("transaction_hash", transaction.getBuyHash());
              view.finish(bundle);
            }))
            .onErrorResumeNext(throwable -> Completable.fromAction(() -> showError(throwable)))
            .andThen(inAppPurchaseInteractor.remove(transaction.getUri()));
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

  public void stop() {
    disposables.clear();
  }

  private void setup(BigDecimal amount) {
    view.setup();
    view.showRaidenChannelValues(inAppPurchaseInteractor.getTopUpChannelSuggestionValues(amount));
  }

  public static class BuyData {
    private final boolean isRaiden;
    private final String uri;
    private final BigDecimal channelBudget;

    public BuyData(boolean isRaiden, String uri, BigDecimal channelBudget) {
      this.isRaiden = isRaiden;
      this.uri = uri;
      this.channelBudget = channelBudget;
    }

    public boolean isRaiden() {
      return isRaiden;
    }

    public String getUri() {
      return uri;
    }

    public BigDecimal getChannelBudget() {
      return channelBudget;
    }
  }
}
