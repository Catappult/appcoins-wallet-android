package com.asf.wallet.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.repository.PendingTransactionService;
import com.asf.wallet.router.GasSettingsRouter;
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
    disposable = sendTransactionInteract.send(transactionBuilder.getValue())
        .flatMapObservable(hash -> pendingTransactionService.checkTransactionState(hash))
        .subscribe(this::onCreateTransaction, this::onError);
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

  public void approve() {
    sendTransactionInteract.approve(transactionBuilder.getValue())
        .subscribe(s -> Log.d(TAG, "approve: " + s), Throwable::printStackTrace);
  }

  public void callSmartContract() {
    sendTransactionInteract.callSmartContract(transactionBuilder.getValue())
        .subscribe(transactionHash -> Log.d(TAG, "callSmartContract: " + transactionHash),
            throwable -> throwable.printStackTrace());
  }
}
