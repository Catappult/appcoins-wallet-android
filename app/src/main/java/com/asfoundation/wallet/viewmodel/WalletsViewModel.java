package com.asfoundation.wallet.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.CreateWalletInteract;
import com.asfoundation.wallet.interact.DeleteWalletInteract;
import com.asfoundation.wallet.interact.ExportWalletInteract;
import com.asfoundation.wallet.interact.FetchWalletsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.SetDefaultWalletInteract;
import com.asfoundation.wallet.logging.Logger;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.router.ImportWalletRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import io.reactivex.disposables.CompositeDisposable;

import static com.asfoundation.wallet.C.IMPORT_REQUEST_CODE;

public class WalletsViewModel extends BaseViewModel {

  private static final String TAG = WalletsViewModel.class.getSimpleName();
  private final CreateWalletInteract createWalletInteract;
  private final SetDefaultWalletInteract setDefaultWalletInteract;
  private final DeleteWalletInteract deleteWalletInteract;
  private final FetchWalletsInteract fetchWalletsInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final ExportWalletInteract exportWalletInteract;
  private final Logger logger;
  private final ImportWalletRouter importWalletRouter;
  private final TransactionsRouter transactionsRouter;
  private final PreferencesRepositoryType preferencesRepositoryType;

  private final MutableLiveData<Wallet[]> wallets = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
  private final MutableLiveData<Wallet> createdWallet = new MutableLiveData<>();
  private final MutableLiveData<ErrorEnvelope> createWalletError = new MutableLiveData<>();
  private final MutableLiveData<String> exportedStore = new MutableLiveData<>();
  private final MutableLiveData<ErrorEnvelope> exportWalletError = new MutableLiveData<>();
  private final MutableLiveData<ErrorEnvelope> deleteWalletError = new MutableLiveData<>();
  private final CompositeDisposable disposables;

  WalletsViewModel(CreateWalletInteract createWalletInteract,
      SetDefaultWalletInteract setDefaultWalletInteract, DeleteWalletInteract deleteWalletInteract,
      FetchWalletsInteract fetchWalletsInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExportWalletInteract exportWalletInteract, ImportWalletRouter importWalletRouter,
      TransactionsRouter transactionsRouter, Logger logger,
      PreferencesRepositoryType preferencesRepositoryType,
      CompositeDisposable compositeDisposable) {
    this.createWalletInteract = createWalletInteract;
    this.setDefaultWalletInteract = setDefaultWalletInteract;
    this.deleteWalletInteract = deleteWalletInteract;
    this.fetchWalletsInteract = fetchWalletsInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.importWalletRouter = importWalletRouter;
    this.exportWalletInteract = exportWalletInteract;
    this.transactionsRouter = transactionsRouter;
    this.logger = logger;
    this.preferencesRepositoryType = preferencesRepositoryType;
    this.disposables = compositeDisposable;

    fetchWallets();
  }

  public LiveData<Wallet[]> wallets() {
    return wallets;
  }

  public LiveData<Wallet> defaultWallet() {
    return defaultWallet;
  }

  public LiveData<Wallet> createdWallet() {
    return createdWallet;
  }

  public LiveData<ErrorEnvelope> createWalletError() {
    return createWalletError;
  }

  public LiveData<String> exportedStore() {
    return exportedStore;
  }

  public LiveData<ErrorEnvelope> exportWalletError() {
    return exportWalletError;
  }

  public LiveData<ErrorEnvelope> deleteWalletError() {
    return deleteWalletError;
  }

  public void setDefaultWallet(Wallet wallet) {
    disposables.add(setDefaultWalletInteract.set(wallet)
        .subscribe(() -> onDefaultWalletChanged(wallet), this::onError));
  }

  public void deleteWallet(Wallet wallet) {
    disposables.add(deleteWalletInteract.delete(wallet)
        .subscribe(this::onFetchWallets, this::onDeleteWalletError));
  }

  private void onFetchWallets(Wallet[] items) {
    progress.postValue(false);
    wallets.postValue(items);
    disposables.add(findDefaultWalletInteract.find()
        .subscribe(this::onDefaultWalletChanged, t -> {
        }));
  }

  private void onDefaultWalletChanged(Wallet wallet) {
    progress.postValue(false);
    defaultWallet.postValue(wallet);
  }

  public void fetchWallets() {
    progress.postValue(true);
    disposables.add(fetchWalletsInteract.fetch()
        .subscribe(this::onFetchWallets, this::onError));
  }

  public void newWallet() {
    progress.setValue(true);
    disposables.add(createWalletInteract.create()
        .map(wallet -> {
          fetchWallets();
          createdWallet.postValue(wallet);
          return wallet;
        })
        .flatMapCompletable(createWalletInteract::setDefaultWallet)
        .subscribe(() -> {
        }, this::onCreateWalletError));
  }

  public void exportWallet(Wallet wallet, String storePassword) {
    disposables.add(exportWalletInteract.export(wallet, storePassword)
        .subscribe(exportedStore::postValue, this::onExportWalletError));
  }

  @Override protected void onCleared() {
    disposables.clear();
    super.onCleared();
  }

  private void onExportWalletError(Throwable throwable) {
    logger.log(TAG, throwable.getMessage(), throwable);
    exportWalletError.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN,
        TextUtils.isEmpty(throwable.getLocalizedMessage()) ? throwable.getMessage()
            : throwable.getLocalizedMessage()));
  }

  private void onDeleteWalletError(Throwable throwable) {
    logger.log(TAG, throwable.getMessage(), throwable);
    deleteWalletError.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN,
        TextUtils.isEmpty(throwable.getLocalizedMessage()) ? throwable.getMessage()
            : throwable.getLocalizedMessage()));
  }

  private void onCreateWalletError(Throwable throwable) {
    throwable.printStackTrace();
    logger.log(TAG, throwable.getMessage(), throwable);
    progress.postValue(false);
    createWalletError.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN, throwable.getMessage()));
  }

  public void importWallet(Activity activity) {
    importWalletRouter.openForResult(activity, IMPORT_REQUEST_CODE);
  }

  public void showTransactions(Context context) {
    transactionsRouter.open(context, true);
  }

  public void clearExportedStore() {
    exportedStore.setValue(null);
  }

  public void saveWalletBackup(String walletAddress) {
    if (walletAddress != null) {
      preferencesRepositoryType.setWalletImportBackup(walletAddress);
    }
  }
}
