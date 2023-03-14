package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.appcoins.wallet.core.utils.properties.HostProperties;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TransactionsDetailsModel;
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase;
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase;
import com.asfoundation.wallet.home.usecases.FindNetworkInfoUseCase;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService;
import com.asfoundation.wallet.subscriptions.SubscriptionActivity;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.iab.FiatValue;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class TransactionDetailViewModel extends BaseViewModel {

  private final ExternalBrowserRouter externalBrowserRouter;
  private final DisplayChatUseCase displayChatUseCase;
  private final TransactionDetailRouter transactionDetailRouter;
  private final LocalCurrencyConversionService conversionService;
  private final FindDefaultWalletUseCase findDefaultWalletUseCase;
  private final FindNetworkInfoUseCase findNetworkInfoUseCase;
  private final MutableLiveData<TransactionsDetailsModel> transactionsDetailsModel =
      new MutableLiveData<>();
  private final CompositeDisposable disposables;

  public TransactionDetailViewModel(FindDefaultWalletUseCase findDefaultWalletUseCase,
      FindNetworkInfoUseCase findNetworkInfoUseCase, ExternalBrowserRouter externalBrowserRouter,
      CompositeDisposable compositeDisposable, DisplayChatUseCase displayChatUseCase,
      TransactionDetailRouter transactionDetailRouter,
      LocalCurrencyConversionService conversionService) {
    this.findDefaultWalletUseCase = findDefaultWalletUseCase;
    this.findNetworkInfoUseCase = findNetworkInfoUseCase;
    this.externalBrowserRouter = externalBrowserRouter;
    this.disposables = compositeDisposable;
    this.displayChatUseCase = displayChatUseCase;
    this.transactionDetailRouter = transactionDetailRouter;
    this.conversionService = conversionService;
  }

  @Override protected void onCleared() {
    disposables.clear();
    super.onCleared();
  }

  public void initializeView(String paidValue, String paidCurrency, String targetCurrency) {
    Single<FiatValue> fiatValueSingle =
        (paidValue != null) ? convertValueToTargetCurrency(paidValue, paidCurrency, targetCurrency)
            : Single.just(new FiatValue());
    disposables.add(Single.zip(findNetworkInfoUseCase.invoke(), findDefaultWalletUseCase.invoke(),
        fiatValueSingle, TransactionsDetailsModel::new)
        .subscribe(transactionsDetailsModel::postValue, t -> {
        }));
  }

  public void showSupportScreen() {
    displayChatUseCase.invoke();
  }

  private Single<FiatValue> convertValueToTargetCurrency(String paidValue, String paidCurrency,
      String targetCurrency) {
    return conversionService.getValueToFiat(paidValue, paidCurrency, targetCurrency, 2)
        .subscribeOn(Schedulers.io());
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
    String url = HostProperties.INSTANCE.getTRANSACTION_DETAILS_HOST();
    return Uri.parse(url)
        .buildUpon()
        .appendEncodedPath(transaction.getTransactionId())
        .build();
  }

  public void showManageSubscriptions(Context context) {
    Intent intent = SubscriptionActivity.newIntent(context);
    context.startActivity(intent);
  }
}
