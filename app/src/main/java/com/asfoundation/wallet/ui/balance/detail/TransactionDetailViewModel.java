package com.asfoundation.wallet.ui.balance.detail;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.appcoins.wallet.bdsbilling.WalletAddressModel;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.support.SupportInteractor;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.viewmodel.BaseViewModel;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TransactionDetailViewModel extends BaseViewModel {

  private final TransactionDetailData data;
  private final TransactionDetailInteractor interactor;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final SupportInteractor supportInteractor;
  private final TransactionDetailRouter transactionDetailRouter;

  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<WalletAddressModel> onWalletAddress = new MutableLiveData<>();
  private final MutableLiveData<VoucherTransactionModel> onVoucherModel = new MutableLiveData<>();
  private final CompositeDisposable disposables;

  TransactionDetailViewModel(TransactionDetailData data, TransactionDetailInteractor interactor,
      FindDefaultNetworkInteract findDefaultNetworkInteract, WalletService walletService,
      ExternalBrowserRouter externalBrowserRouter, CompositeDisposable compositeDisposable,
      SupportInteractor supportInteractor, TransactionDetailRouter transactionDetailRouter) {
    this.data = data;
    this.interactor = interactor;
    this.externalBrowserRouter = externalBrowserRouter;
    this.disposables = compositeDisposable;
    this.supportInteractor = supportInteractor;
    this.transactionDetailRouter = transactionDetailRouter;
    disposables.add(findDefaultNetworkInteract.find()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(defaultNetwork::postValue, t -> {
        }));
    disposables.add(walletService.getAndSignCurrentWalletAddress()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(onWalletAddress::postValue)
        .observeOn(Schedulers.io())
        .flatMap(walletAddressModel -> {
          if (data.getTransaction()
              .getType() == Transaction.TransactionType.VOUCHER) {
            return retrieveAndShowVoucher(walletAddressModel).map(
                voucherTransactionModel -> walletAddressModel);
          }
          return Single.just(walletAddressModel);
        })
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private Single<VoucherTransactionModel> retrieveAndShowVoucher(
      WalletAddressModel walletAddressModel) {
    return interactor.getVoucherTransactionModel(data.getTransaction()
        .getTransactionId(), walletAddressModel.getAddress(), walletAddressModel.getSignedAddress())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(onVoucherModel::postValue);
  }

  @Override protected void onCleared() {
    disposables.clear();
    super.onCleared();
  }

  public void showSupportScreen() {
    supportInteractor.displayChatScreen();
  }

  public void showDetails(Context context, Transaction transaction) {
    transactionDetailRouter.open(context, transaction);
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

  public LiveData<WalletAddressModel> onWalletAddress() {
    return onWalletAddress;
  }

  public LiveData<VoucherTransactionModel> onVoucherModel() {
    return onVoucherModel;
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
