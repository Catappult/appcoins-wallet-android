package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TransactionsDetailsModel;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService;
import com.asfoundation.wallet.support.SupportInteractor;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TransactionDetailViewModel extends BaseViewModel {

  private final ExternalBrowserRouter externalBrowserRouter;
  private final SupportInteractor supportInteractor;
  private final TransactionDetailRouter transactionDetailRouter;
  private final LocalCurrencyConversionService conversionService;
  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final MutableLiveData<TransactionsDetailsModel> transactionsDetailsModel =
      new MutableLiveData<>();
  private final CompositeDisposable disposables;

  TransactionDetailViewModel(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, CompositeDisposable compositeDisposable,
      SupportInteractor supportInteractor, TransactionDetailRouter transactionDetailRouter,
      LocalCurrencyConversionService conversionService) {
    this.externalBrowserRouter = externalBrowserRouter;
    this.disposables = compositeDisposable;
    this.supportInteractor = supportInteractor;
    this.transactionDetailRouter = transactionDetailRouter;
    this.conversionService = conversionService;
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
  }

  @Override protected void onCleared() {
    disposables.clear();
    super.onCleared();
  }

  public void initializeView(String paidValue, String paidCurrency) {
    disposables.add(Single.zip(findDefaultNetworkInteract.find(), findDefaultWalletInteract.find(),
        getConvertedValue(paidValue, paidCurrency), TransactionsDetailsModel::new)
        .subscribe(transactionsDetailsModel::postValue, t -> {
        }));
  }

  public void showSupportScreen() {
    supportInteractor.displayChatScreen();
  }

  private Observable<FiatValue> convertValue(String value, String currency, int scale) {
    return conversionService.getValueToFiat(value, currency, scale);
  }

  public Single<String> getConvertedValue(String value, String currency) {
    return convertValue(value, currency, 2).subscribeOn(Schedulers.io())
        .firstElement()
        .flatMapSingle(converted -> Single.just(converted.getAmount()
            .toString()));
  }

  public LiveData<TransactionsDetailsModel> transactionsDetailsModel() {
    return transactionsDetailsModel;
  }

  public void showDetails(Context context, Transaction transaction, String globalBalanceCurrency) {
    transactionDetailRouter.open(context, transaction, globalBalanceCurrency);
  }

  public void showMoreDetails(Context context, Operation transaction) {
    Uri uri = buildEtherscanUri(transaction);
    if (uri != null) {
      externalBrowserRouter.open(context, uri);
    }
  }

  @Nullable private Uri buildEtherscanUri(Operation operation) {
    NetworkInfo networkInfo = transactionsDetailsModel.getValue()
        .getNetworkInfo();
    if (networkInfo != null && !TextUtils.isEmpty(networkInfo.etherscanUrl)) {
      return Uri.parse(networkInfo.etherscanUrl)
          .buildUpon()
          .appendEncodedPath(operation.getTransactionId())
          .build();
    }
    return null;
  }

  public void showMoreDetailsBds(Context context, Transaction transaction) {
    Uri uri = buildBdsUri(transaction);
    if (uri != null) {
      externalBrowserRouter.open(context, uri);
    }
  }

  private Uri buildBdsUri(Transaction transaction) {
    NetworkInfo networkInfo = transactionsDetailsModel.getValue()
        .getNetworkInfo();
    String url = networkInfo.chainId == 3 ? BuildConfig.TRANSACTION_DETAILS_HOST_ROPSTEN
        : BuildConfig.TRANSACTION_DETAILS_HOST;
    return Uri.parse(url)
        .buildUpon()
        .appendEncodedPath(transaction.getTransactionId())
        .build();
  }
}
