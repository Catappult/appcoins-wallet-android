package com.asfoundation.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.ui.MicroRaidenInteractor;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TransactionDetailViewModel extends BaseViewModel {

  private final ExternalBrowserRouter externalBrowserRouter;

  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();

  private final MicroRaidenInteractor microRaidenInteractor;

  TransactionDetailViewModel(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, MicroRaidenInteractor interactor) {
    this.externalBrowserRouter = externalBrowserRouter;
    this.microRaidenInteractor = interactor;
    findDefaultNetworkInteract.find()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(defaultNetwork::postValue, t -> {
        });
    disposable = findDefaultWalletInteract.find()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(defaultWallet::postValue, t -> {
        });
  }

  public LiveData<NetworkInfo> defaultNetwork() {
    return defaultNetwork;
  }

  public void showMoreDetails(Context context, Operation transaction) {
    Uri uri = buildEtherscanUri(transaction);
    if (uri != null) {
      externalBrowserRouter.open(context, uri);
    }
  }

  @Nullable private Uri buildEtherscanUri(Operation operation) {
    NetworkInfo networkInfo = defaultNetwork.getValue();
    if (networkInfo != null && !TextUtils.isEmpty(networkInfo.etherscanUrl)) {
      return Uri.parse(networkInfo.etherscanUrl)
          .buildUpon()
          .appendEncodedPath(operation.getTransactionId())
          .build();
    }
    return null;
  }

  public LiveData<Wallet> defaultWallet() {
    return defaultWallet;
  }

  public Completable closeChannel(String fromAddress) {
      return microRaidenInteractor.closeChannel(fromAddress)
          .subscribeOn(Schedulers.io());
  }

}
