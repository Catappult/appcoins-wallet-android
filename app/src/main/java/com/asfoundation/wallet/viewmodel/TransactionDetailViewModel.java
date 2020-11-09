package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.content.Intent;
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
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.subscriptions.SubscriptionActivity;
import com.asfoundation.wallet.subscriptions.SubscriptionDetails;
import com.asfoundation.wallet.subscriptions.SubscriptionRepository;
import com.asfoundation.wallet.support.SupportInteractor;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class TransactionDetailViewModel extends BaseViewModel {

  private final ExternalBrowserRouter externalBrowserRouter;
  private final SupportInteractor supportInteractor;
  private final TransactionDetailRouter transactionDetailRouter;
  private final SubscriptionRepository subscriptionRepository;
  private final Scheduler networkScheduler;
  private final Scheduler viewScheduler;

  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
  private final MutableLiveData<SubscriptionDetails> subscriptionDetails = new MutableLiveData<>();
  private final CompositeDisposable disposables;

  TransactionDetailViewModel(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, CompositeDisposable compositeDisposable,
      SupportInteractor supportInteractor, TransactionDetailRouter transactionDetailRouter,
      SubscriptionRepository subscriptionRepository, Scheduler networkScheduler,
      Scheduler viewScheduler) {
    this.externalBrowserRouter = externalBrowserRouter;
    this.disposables = compositeDisposable;
    this.supportInteractor = supportInteractor;
    this.transactionDetailRouter = transactionDetailRouter;
    this.subscriptionRepository = subscriptionRepository;
    this.networkScheduler = networkScheduler;
    this.viewScheduler = viewScheduler;
    disposables.add(findDefaultNetworkInteract.find()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(defaultNetwork::postValue, t -> {
        }));
    disposables.add(findDefaultWalletInteract.find()
        .observeOn(viewScheduler)
        .subscribe(defaultWallet::postValue, t -> {
        }));
  }

  @Override protected void onCleared() {
    disposables.clear();
    super.onCleared();
  }

  public void loadSubscriptionDetails(String transactionId) {
    disposable = subscriptionRepository.getSubscriptionByTrxId(transactionId)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe(subscriptionDetails::postValue, t -> {
        });
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

  public MutableLiveData<SubscriptionDetails> subscriptionDetails() {
    return subscriptionDetails;
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

  public void showManageSubscriptions(Context context) {
    Intent intent = SubscriptionActivity.newIntent(context, SubscriptionActivity.ACTION_LIST);
    context.startActivity(intent);
  }

  public void cancelSubscription(Context context, String packageName) {
    Intent intent =
        SubscriptionActivity.newIntent(context, SubscriptionActivity.ACTION_CANCEL, packageName);
    context.startActivity(intent);
  }
}
