package com.asf.wallet.ui.iab;

import android.util.Log;
import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.repository.TransactionService;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabPresenter {
  private static final String TAG = IabPresenter.class.getSimpleName();
  private final IabView view;
  private final TransactionService transactionService;
  private final Scheduler viewScheduler;
  private Disposable disposable;

  public IabPresenter(IabView view, TransactionService transactionService,
      Scheduler viewScheduler) {
    this.view = view;
    this.transactionService = transactionService;
    this.viewScheduler = viewScheduler;
  }

  public void present(String uriString) {
    transactionService.parseTransaction(uriString)
        .observeOn(viewScheduler)
        .subscribe(transactionBuilder -> view.setup(transactionBuilder), this::showError);

    disposable = view.getBuyClick()
        .doOnNext(__ -> view.lockOrientation())
        .flatMap(uri -> transactionService.sendTransaction(uri)
            .observeOn(viewScheduler)
            .doOnNext(this::showPendingTransaction)
            .doOnError(this::showError))
        .retry()
        .subscribe();
  }

  private void showError(Throwable throwable) {
    view.unlockOrientation();
    view.showError();
  }

  private void showPendingTransaction(PendingTransaction pendingTransaction) {
    Log.d(TAG, "present: " + pendingTransaction);
    if (!pendingTransaction.isPending()) {
      view.finish(pendingTransaction.getHash());
      view.unlockOrientation();
    } else {
      view.showLoading();
    }
  }

  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
