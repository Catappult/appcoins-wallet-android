package com.asfoundation.wallet.viewmodel;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.crashlytics.android.Crashlytics;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ConfirmationViewModel extends BaseViewModel {
  private final MutableLiveData<TransactionBuilder> transactionBuilder = new MutableLiveData<>();
  private final MutableLiveData<PendingTransaction> transactionHash = new MutableLiveData<>();
  private final SendTransactionInteract sendTransactionInteract;
  private final GasSettingsRouter gasSettingsRouter;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private Disposable subscription;

  ConfirmationViewModel(SendTransactionInteract sendTransactionInteract,
      GasSettingsRouter gasSettingsRouter, FetchGasSettingsInteract gasSettingsInteract) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.gasSettingsRouter = gasSettingsRouter;
    this.gasSettingsInteract = gasSettingsInteract;
  }

  public void init(TransactionBuilder transactionBuilder) {
    subscription = gasSettingsInteract.fetch(transactionBuilder.shouldSendToken())
        .doOnSuccess(gasSettings -> {
          transactionBuilder.gasSettings(gasSettings);
          this.transactionBuilder.postValue(transactionBuilder);
        })
        .subscribe(__ -> {
        }, this::onError);
  }

  @Override protected void onCleared() {
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
    }
    super.onCleared();
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
        .map(hash -> new PendingTransaction(hash, false))
        .observeOn(AndroidSchedulers.mainThread())
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
