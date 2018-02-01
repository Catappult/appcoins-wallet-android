package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.google.android.gms.vision.barcode.Barcode;
import com.wallet.crypto.trustapp.entity.Address;
import com.wallet.crypto.trustapp.entity.TransactionBuilder;
import com.wallet.crypto.trustapp.router.ConfirmationRouter;
import com.wallet.crypto.trustapp.util.BalanceUtils;
import com.wallet.crypto.trustapp.util.QRUri;

public class SendViewModel extends BaseViewModel {
    private final MutableLiveData<String> symbol = new MutableLiveData<>();
    private final MutableLiveData<String> address = new MutableLiveData<>();

    private TransactionBuilder transactionBuilder;

    private final ConfirmationRouter confirmationRouter;

    SendViewModel(ConfirmationRouter confirmationRouter) {
        this.confirmationRouter = confirmationRouter;
    }

    public void openConfirmation(Context context) {
        confirmationRouter.open(context, transactionBuilder);
    }

    public void setTransactionBuilder(TransactionBuilder transactionBuilder) {
        this.transactionBuilder = transactionBuilder;
        symbol.postValue(transactionBuilder.symbol());
        address.postValue(transactionBuilder.toAddress());
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

    public boolean setAmmount(String amount) {
        try {
            transactionBuilder.amount(BalanceUtils.EthToWei(amount));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean extractFromQR(Barcode barcode) {
        QRUri qrUrl = QRUri.parse(barcode.displayValue);
        if (qrUrl != null) {
            transactionBuilder.toAddress(qrUrl.getAddress());
            transactionBuilder.tokenData(qrUrl.getParameter("data"));
            address.postValue(qrUrl.getAddress());
            return true;
        } else {
            return false;
        }
    }
}
