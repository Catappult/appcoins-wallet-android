package com.asf.wallet.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.router.GasSettingsRouter;
import com.crashlytics.android.Crashlytics;

public class ConfirmationViewModel extends BaseViewModel {
  private final MutableLiveData<TransactionBuilder> transactionBuilder = new MutableLiveData<>();
  private final MutableLiveData<String> transactionHash = new MutableLiveData<>();

  private final SendTransactionInteract sendTransactionInteract;

  private final GasSettingsRouter gasSettingsRouter;

  ConfirmationViewModel(SendTransactionInteract sendTransactionInteract,
      GasSettingsRouter gasSettingsRouter) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.gasSettingsRouter = gasSettingsRouter;
  }

  public void init(TransactionBuilder transactionBuilder) {
    this.transactionBuilder.postValue(transactionBuilder);
  }

  public LiveData<TransactionBuilder> transactionBuilder() {
    return transactionBuilder;
  }

  public LiveData<String> transactionHash() {
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

  private void onCreateTransaction(String transaction) {
    progress.postValue(false);
    transactionHash.postValue(transaction);
  }

  public void send() {
    progress.postValue(true);
    disposable = sendTransactionInteract.send(transactionBuilder.getValue())
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
}
