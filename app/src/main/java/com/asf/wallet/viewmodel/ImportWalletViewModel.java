package com.asf.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.asf.wallet.C;
import com.asf.wallet.entity.ErrorEnvelope;
import com.asf.wallet.entity.ServiceErrorException;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.interact.ImportWalletInteract;
import com.asf.wallet.ui.widget.OnImportKeystoreListener;
import com.asf.wallet.ui.widget.OnImportPrivateKeyListener;

public class ImportWalletViewModel extends BaseViewModel
    implements OnImportKeystoreListener, OnImportPrivateKeyListener {

  private final ImportWalletInteract importWalletInteract;
  private final MutableLiveData<Wallet> wallet = new MutableLiveData<>();

  ImportWalletViewModel(ImportWalletInteract importWalletInteract) {
    this.importWalletInteract = importWalletInteract;
  }

  @Override public void onKeystore(String keystore, String password) {
    progress.postValue(true);
    importWalletInteract.importKeystore(keystore, password)
        .subscribe(this::onWallet, this::onError);
  }

  @Override public void onPrivateKey(String key) {
    progress.postValue(true);
    importWalletInteract.importPrivateKey(key)
        .subscribe(this::onWallet, this::onError);
  }

  public LiveData<Wallet> wallet() {
    return wallet;
  }

  private void onWallet(Wallet wallet) {
    progress.postValue(false);
    this.wallet.postValue(wallet);
  }

  public void onError(Throwable throwable) {
    if (throwable.getCause() instanceof ServiceErrorException) {
      if (((ServiceErrorException) throwable.getCause()).code == C.ErrorCode.ALREADY_ADDED) {
        error.postValue(new ErrorEnvelope(C.ErrorCode.ALREADY_ADDED, null));
      }
    } else {
      error.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN, throwable.getMessage()));
    }
  }
}
