package com.asf.wallet.ui.iab;

import android.util.Log;
import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.repository.TransactionService;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.util.concurrent.TimeUnit;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabPresenter {
  private static final String TAG = IabPresenter.class.getSimpleName();
  private final IabView view;
  private final TransactionService transactionService;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;

  public IabPresenter(IabView view, TransactionService transactionService, Scheduler viewScheduler,
      CompositeDisposable disposables) {
    this.view = view;
    this.transactionService = transactionService;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
  }

  public void present(String uriString) {
    disposables.add(transactionService.parseTransaction(uriString)
        .observeOn(viewScheduler)
        .subscribe(transactionBuilder -> view.setup(transactionBuilder), this::showError));

    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));

    disposables.add(view.getOkErrorClick()
        .subscribe(click -> showBuy()));

    disposables.add(view.getBuyClick()
        .doOnNext(__ -> view.lockOrientation())
        .flatMapCompletable(uri -> transactionService.sendTransaction(uri)
            .observeOn(viewScheduler)
            .flatMapCompletable(this::showPendingTransaction)
            .doOnError(this::showError))
        .retry()
        .subscribe());
  }

  private void showBuy() {
    view.showBuy();
  }

  private void close() {
    view.close();
  }

  private void showError(Throwable throwable) {
    throwable.printStackTrace();
    view.unlockOrientation();
    view.showError();
  }

  private Completable showPendingTransaction(PendingTransaction pendingTransaction) {
    Log.d(TAG, "present: " + pendingTransaction);
    if (pendingTransaction.isPending()) {
      return Completable.fromAction(view::showLoading);
    } else {
      return Completable.fromAction(view::showTransactionCompleted)
          .andThen(Completable.timer(1, TimeUnit.SECONDS))
          .andThen(Completable.fromAction(() -> {
            view.finish(pendingTransaction.getHash());
            view.unlockOrientation();
          }));
    }
  }

  public void stop() {
    disposables.clear();
  }
}
