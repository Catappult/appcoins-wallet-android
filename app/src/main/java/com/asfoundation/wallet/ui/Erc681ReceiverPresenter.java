package com.asfoundation.wallet.ui;

import android.os.Bundle;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.disposables.Disposable;

class Erc681ReceiverPresenter {
  private final Erc681ReceiverView view;
  private final TransferParser transferParser;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final FindDefaultWalletInteract walletInteract;
  private final String data;
  private Disposable disposable;

  public Erc681ReceiverPresenter(Erc681ReceiverView view, TransferParser transferParser,
      InAppPurchaseInteractor inAppPurchaseInteractor, FindDefaultWalletInteract walletInteract,
      String data) {
    this.view = view;
    this.transferParser = transferParser;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.walletInteract = walletInteract;
    this.data = data;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      disposable = walletInteract.find()
          .flatMap(__ -> transferParser.parse(data))
          .map(transactionBuilder -> {
            String callingPackage = transactionBuilder.getDomain();
            if (callingPackage == null) {
              callingPackage = view.getCallingPackage();
            }
            transactionBuilder.setDomain(callingPackage);
            return transactionBuilder;
          })
          .flatMap(transactionBuilder -> inAppPurchaseInteractor.isWalletFromBds(
              transactionBuilder.getDomain(), transactionBuilder.toAddress())
              .doOnSuccess(isBds -> view.startEipTransfer(transactionBuilder, isBds)))
          .subscribe(transaction -> {
          }, throwable -> view.startApp(throwable));
    }
  }

  public void pause() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
