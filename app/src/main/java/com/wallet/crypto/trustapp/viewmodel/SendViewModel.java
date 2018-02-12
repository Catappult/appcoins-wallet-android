package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import com.google.android.gms.vision.barcode.Barcode;
import com.wallet.crypto.trustapp.entity.Address;
import com.wallet.crypto.trustapp.entity.GasSettings;
import com.wallet.crypto.trustapp.entity.TransactionBuilder;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.interact.FetchGasSettingsInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.router.ConfirmationRouter;
import com.wallet.crypto.trustapp.util.QRUri;
import java.math.BigDecimal;
import org.web3j.utils.Numeric;

public class SendViewModel extends BaseViewModel {
  private final MutableLiveData<String> symbol = new MutableLiveData<>();
  private final MutableLiveData<String> address = new MutableLiveData<>();
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchGasSettingsInteract fetchGasSettingsInteract;
  private final ConfirmationRouter confirmationRouter;
  private TransactionBuilder transactionBuilder;

  SendViewModel(FindDefaultWalletInteract findDefaultWalletInteract,
      FetchGasSettingsInteract fetchGasSettingsInteract, ConfirmationRouter confirmationRouter) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchGasSettingsInteract = fetchGasSettingsInteract;
    this.confirmationRouter = confirmationRouter;
  }

  public void init(TransactionBuilder transactionBuilder) {
    this.transactionBuilder = transactionBuilder;
    symbol.postValue(transactionBuilder.symbol());
    address.postValue(transactionBuilder.toAddress());
    fetchGasSettingsInteract.fetch(transactionBuilder.shouldSendToken())
        .subscribe(this::onGasSettings, t -> {
        });
    disposable = findDefaultWalletInteract.find()
        .subscribe(this::onDefaultWallet, this::onError);
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
      transactionBuilder.amount(new BigDecimal(amount));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean extractFromQR(Barcode barcode) {
    QRUri qrUrl = QRUri.parse(barcode.displayValue);
    if (qrUrl != null) {
      transactionBuilder.toAddress(qrUrl.getAddress());
      transactionBuilder.data(
          Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(qrUrl.getParameter("data"))));
      address.postValue(qrUrl.getAddress());
      return true;
    } else {
      return false;
    }
  }

  public void openConfirmation(Context context) {
    confirmationRouter.open(context, transactionBuilder);
  }

  private void onGasSettings(GasSettings gasSettings) {
    transactionBuilder.gasSettings(gasSettings);
  }

  private void onDefaultWallet(Wallet wallet) {
    transactionBuilder.fromAddress(wallet.address);
  }
}
