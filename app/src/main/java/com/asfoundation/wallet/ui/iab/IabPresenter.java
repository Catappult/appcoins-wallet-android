package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethod;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabPresenter {

  private static final String DEFAULT_CURRENCY = "EUR";

  private final IabView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final boolean isBds;
  private final TransactionBuilder transaction;

  public IabPresenter(IabView view, InAppPurchaseInteractor inAppPurchaseInteractor,
      Scheduler viewScheduler, CompositeDisposable disposables, boolean isBds,
      TransactionBuilder transaction) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.isBds = isBds;
    this.transaction = transaction;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      setupUi();
    }
  }

  private void setupUi() {
    if (isBds) {
      disposables.add(showBdsPayment(transaction).subscribe(() -> {
      }, this::showError));
    } else {
      disposables.add(Completable.fromAction(() -> view.showOnChain(transaction.amount(), false))
          .subscribe(() -> {
          }, this::showError));
    }
  }

  private Completable showBdsPayment(TransactionBuilder transactionBuilder) {
    return inAppPurchaseInteractor.getCurrentPaymentStep(transactionBuilder.getDomain(),
        transactionBuilder)
        .flatMapCompletable(currentPaymentStep -> {
          switch (currentPaymentStep) {
            case PAUSED_CC_PAYMENT:
              return Completable.fromAction(
                  () -> view.showCcPayment(transactionBuilder.amount(), DEFAULT_CURRENCY, true));
            case PAUSED_ON_CHAIN:
              return Completable.fromAction(
                  () -> view.showOnChain(transactionBuilder.amount(), isBds));
            case NO_FUNDS:
            case READY:
            default:
              return inAppPurchaseInteractor.getPaymentMethods()
                  .subscribeOn(Schedulers.io())
                  .observeOn(viewScheduler)
                  .doOnSuccess(
                      paymentMethods -> view.showPaymentMethods(mapPaymentMethods(paymentMethods)))
                  .ignoreElement();
          }
        });
  }

  private List<com.asfoundation.wallet.ui.iab.PaymentMethod> mapPaymentMethods(
      List<PaymentMethod> paymentMethods) {
    List<com.asfoundation.wallet.ui.iab.PaymentMethod> list = new ArrayList<>();
    for (PaymentMethod paymentMethod : paymentMethods) {
      list.add(new com.asfoundation.wallet.ui.iab.PaymentMethod(paymentMethod.getId(),
          paymentMethod.getLabel(), paymentMethod.getIconUrl(), true));
    }
    return list;
  }

  private void showError(@Nullable Throwable throwable) {
    if (throwable != null) {
      throwable.printStackTrace();
    }
  }

  public void stop() {
    disposables.clear();
  }
}