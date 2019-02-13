package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class TransactionDetailViewModel extends BaseViewModel {

  private final ExternalBrowserRouter externalBrowserRouter;

  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();

  TransactionDetailViewModel(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter) {
    this.externalBrowserRouter = externalBrowserRouter;
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

  public void showMoreDetailsBds(Context context, Transaction transaction) {
    Uri uri = buildBdsUri(transaction);
    if (uri != null) {
      externalBrowserRouter.open(context, uri);
    }
  }

  private Uri buildBdsUri(Transaction transaction) {
    NetworkInfo networkInfo = defaultNetwork.getValue();
    String url = networkInfo.chainId == 3 ? BuildConfig.TRANSACTION_DETAILS_HOST_ROPSTEN
        : BuildConfig.TRANSACTION_DETAILS_HOST;
    return Uri.parse(url)
        .buildUpon()
        .appendEncodedPath(transaction.getTransactionId())
        .build();
  }
}
