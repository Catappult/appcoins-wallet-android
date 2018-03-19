package com.asfoundation.wallet.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.crashlytics.android.Crashlytics;

public class ConfirmationViewModel extends BaseViewModel {
  private static final String TAG = ConfirmationViewModel.class.getSimpleName();
  private final MutableLiveData<TransactionBuilder> transactionBuilder = new MutableLiveData<>();
  private final MutableLiveData<PendingTransaction> transactionHash = new MutableLiveData<>();
  private final SendTransactionInteract sendTransactionInteract;
  private final GasSettingsRouter gasSettingsRouter;
  private final PendingTransactionService pendingTransactionService;

  ConfirmationViewModel(SendTransactionInteract sendTransactionInteract,
      GasSettingsRouter gasSettingsRouter, PendingTransactionService pendingTransactionService) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.gasSettingsRouter = gasSettingsRouter;
    this.pendingTransactionService = pendingTransactionService;
  }

  public void init(TransactionBuilder transactionBuilder) {
    this.transactionBuilder.postValue(transactionBuilder);
  }

  public LiveData<TransactionBuilder> transactionBuilder() {
    return transactionBuilder;
  }

  public LiveData<PendingTransaction> transactionHash() {
    return transactionHash;
  }

  public void openGasSettings(Activity context) {
    TransactionBuilder transactionBuilder = this.transactionBuilder.getValue();
    if (transactionBuilder != null) {
      gasSettingsRouter.open(context, transactionBuilder.gasSettings());
    }/* else {
        // TODO: Good idea return to SendActivity
        }*/
  }

  private void onCreateTransaction(PendingTransaction pendingTransaction) {
    transactionHash.postValue(pendingTransaction);
  }

  public void send() {
    progress.postValue(true);
    switch (transactionBuilder.getValue()
        .getTransactionType()) {
      case APPC:
        // TODO: 3/11/18 trinkes refactor this. We don't all the appcoins transactions to be a buy
        disposable = sendTransactionInteract.approve(transactionBuilder.getValue())
            .doOnSuccess(
                approveHash -> onCreateTransaction(new PendingTransaction(approveHash, true)))
            .flatMapObservable(pendingTransactionService::checkTransactionState)
            .filter(pendingTransaction -> !pendingTransaction.isPending())
            .flatMapSingle(approved -> sendTransactionInteract.buy(transactionBuilder.getValue()))
            .flatMap(pendingTransactionService::checkTransactionState)
            .subscribe(this::onCreateTransaction, this::onError);

        break;
      case TOKEN:
      case ETH:
        disposable = sendTransactionInteract.send(transactionBuilder.getValue())
            .flatMapObservable(pendingTransactionService::checkTransactionState)
            .subscribe(this::onCreateTransaction, this::onError);
    }
  }

  public void setGasSettings(GasSettings gasSettings) {
    TransactionBuilder transactionBuilder = this.transactionBuilder.getValue();
    if (transactionBuilder != null) {
      transactionBuilder.gasSettings(gasSettings);
      this.transactionBuilder.postValue(transactionBuilder); // refresh view
    }/* else {
        // TODO: Good idea return to SendActivity
        }*/
  }

  @Override protected void onError(Throwable throwable) {
    super.onError(throwable);
    Crashlytics.logException(throwable);
  }
}
