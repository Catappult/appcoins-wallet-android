package com.asf.wallet.ui.iab;

import android.util.Log;
import com.asf.wallet.repository.TransactionService;
import io.reactivex.disposables.Disposable;

/**
 * Created by trinkes on 13/03/2018.
 */

public class IabPresenter {
  private static final String TAG = IabPresenter.class.getSimpleName();
  private final IabView view;
  private final TransactionService transactionService;
  private Disposable disposable;

  public IabPresenter(IabView view, TransactionService transactionService) {
    this.view = view;
    this.transactionService = transactionService;
  }

  public void present() {
    disposable = view.getBuyClick()
        .flatMap(transactionService::sendTransaction)
        .subscribe(pendingTransaction -> {
          Log.d(TAG, "present: " + pendingTransaction);
          if (!pendingTransaction.isPending()) {
            view.finish(pendingTransaction.getHash());
          }
        });
  }

  public void stop() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
