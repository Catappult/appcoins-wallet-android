package com.asfoundation.wallet.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.entity.Address;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.router.Result;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.router.TransferConfirmationRouter;
import com.asfoundation.wallet.util.QRUri;
import com.asfoundation.wallet.util.TransferParser;
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract;
import com.google.android.gms.vision.barcode.Barcode;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import org.web3j.utils.Numeric;

import static com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity.ERROR_CODE;

public class SendViewModel extends BaseViewModel {
  private final MutableLiveData<String> symbol = new MutableLiveData<>();
  private final MutableLiveData<String> address = new MutableLiveData<>();
  private final MutableLiveData<BigDecimal> amount = new MutableLiveData<>();
  private final MutableLiveData<Result> transactionSucceed = new MutableLiveData<>();
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchGasSettingsInteract fetchGasSettingsInteract;
  private final TransferConfirmationRouter transferConfirmationRouter;
  private final TransferParser transferParser;
  private final CompositeDisposable disposables;
  private final TransactionsRouter transactionsRouter;
  private TransactionBuilder transactionBuilder;

  SendViewModel(FindDefaultWalletInteract findDefaultWalletInteract,
      FetchGasSettingsInteract fetchGasSettingsInteract,
      TransferConfirmationRouter transferConfirmationRouter, TransferParser transferParser,
      TransactionsRouter transactionsRouter) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchGasSettingsInteract = fetchGasSettingsInteract;
    this.transferConfirmationRouter = transferConfirmationRouter;
    this.transferParser = transferParser;
    this.transactionsRouter = transactionsRouter;
    disposables = new CompositeDisposable();
  }

  @Override protected void onCleared() {
    disposables.clear();
    super.onCleared();
  }

  public void init(TransactionBuilder transactionBuilder, Uri data) {
    if (transactionBuilder != null) {
      this.transactionBuilder = transactionBuilder;
      symbol.postValue(transactionBuilder.symbol());
      disposables.add(fetchGasSettingsInteract.fetch(transactionBuilder.shouldSendToken())
          .subscribe(this::onGasSettings, this::onError));

      disposables.add(findDefaultWalletInteract.find()
          .subscribe(this::onDefaultWallet, this::onError));
    } else {
      disposables.add(transferParser.parse(data.toString())
          .flatMapObservable(transaction -> {
            this.transactionBuilder = transaction;
            symbol.postValue(transaction.symbol());
            address.postValue(transaction.toAddress());
            amount.postValue(transaction.amount());
            return fetchGasSettingsInteract.fetch(transaction.shouldSendToken())
                .doOnSuccess(this::onGasSettings)
                .flatMap(gasSettings -> findDefaultWalletInteract.find()
                    .doOnSuccess(this::onDefaultWallet))
                .flatMapObservable(wallet -> transferConfirmationRouter.getTransactionResult()
                    .doOnNext(transactionSucceed::postValue));
          })
          .subscribe(wallet -> {
          }, this::onError));
    }
  }

  public MutableLiveData<Result> onTransactionSucceed() {
    return transactionSucceed;
  }

  public MutableLiveData<BigDecimal> amount() {
    return amount;
  }

  public LiveData<String> symbol() {
    return symbol;
  }

  public LiveData<String> toAddress() {
    return address;
  }

  public boolean setToAddress(String toAddress) {
    if (Address.isAddress(toAddress)) {
      transactionBuilder.toAddress(toAddress);
      return true;
    } else {
      return false;
    }
  }

  public boolean setAmount(String amount) {
    try {
      BigDecimal value = new BigDecimal(amount);
      transactionBuilder.amount(value);
      this.amount.postValue(value);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean extractFromQR(Barcode barcode) {
    QRUri qrUrl = QRUri.parse(barcode.displayValue);
    if (!qrUrl.getAddress()
        .equals(ERROR_CODE)) {
      transactionBuilder.toAddress(qrUrl.getAddress());
      if (qrUrl.getParameter("data") != null) {
        transactionBuilder.data(
            Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(qrUrl.getParameter("data"))));
      }
      address.postValue(qrUrl.getAddress());
      return true;
    } else {
      return false;
    }
  }

  public void openConfirmation(Activity activity) {
    transferConfirmationRouter.open(activity, transactionBuilder);
  }

  private void onGasSettings(GasSettings gasSettings) {
    transactionBuilder.gasSettings(gasSettings);
  }

  private void onDefaultWallet(Wallet wallet) {
    transactionBuilder.fromAddress(wallet.getAddress());
  }

  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    return transferConfirmationRouter.onActivityResult(requestCode, resultCode, data);
  }
}