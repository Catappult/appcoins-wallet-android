package com.asfoundation.wallet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.ServiceErrorException;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.ui.widget.OnImportKeystoreListener;
import com.asfoundation.wallet.ui.widget.OnImportPrivateKeyListener;
import io.reactivex.Completable;

public class ImportWalletViewModel extends BaseViewModel
    implements OnImportKeystoreListener, OnImportPrivateKeyListener {

  private final ImportWalletInteract importWalletInteract;
  private final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
  private final WalletRepositoryType walletRepository;

  ImportWalletViewModel(ImportWalletInteract importWalletInteract,
      WalletRepositoryType walletRepository) {
    this.importWalletInteract = importWalletInteract;
    this.walletRepository = walletRepository;
  }

  @Override public void onKeystore(String keystore, String password) {
    progress.postValue(true);
    importWalletInteract.importKeystore(keystore, password)
        .flatMapCompletable(wallet -> walletRepository.setDefaultWallet(wallet)
            .andThen(Completable.fromAction(() -> onWallet(wallet))))
        .subscribe(() -> {
        }, this::onError);
  }

  @Override public void onPrivateKey(String key) {
    progress.postValue(true);
    importWalletInteract.importPrivateKey(key)
        .flatMapCompletable(wallet -> walletRepository.setDefaultWallet(wallet)
            .andThen(Completable.fromAction(() -> onWallet(wallet))))
        .subscribe(() -> {
        }, this::onError);
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
