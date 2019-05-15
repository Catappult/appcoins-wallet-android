package com.asfoundation.wallet.ui;

import android.os.Bundle;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.PaymentReceiverInteract;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

class Erc681ReceiverPresenter {
  private final Erc681ReceiverView view;
  private final TransferParser transferParser;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final FindDefaultWalletInteract walletInteract;
  private final String data;
  private final PaymentReceiverInteract paymentReceiverInteract;
  private Disposable disposable;

  public Erc681ReceiverPresenter(Erc681ReceiverView view, TransferParser transferParser,
      InAppPurchaseInteractor inAppPurchaseInteractor, FindDefaultWalletInteract walletInteract,
      String data, PaymentReceiverInteract paymentReceiverInteract) {
    this.view = view;
    this.transferParser = transferParser;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.walletInteract = walletInteract;
    this.data = data;
    this.paymentReceiverInteract = paymentReceiverInteract;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      disposable = walletInteract.find()
          .onErrorResumeNext(throwable -> throwable instanceof WalletNotFoundException
              ? createWallet().doAfterTerminate(view::endAnimation) : Single.error(throwable))
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
              .doOnSuccess(isBds -> view.startEipTransfer(transactionBuilder, isBds,
                  transactionBuilder.getPayload())))
          .subscribe(transaction -> {
          }, view::startApp);
    }
  }

  public void pause() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private Single<Wallet> createWallet() {
    view.showLoadingAnimation();
    return paymentReceiverInteract.createWallet();
  }
}
