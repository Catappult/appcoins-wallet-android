package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Single;

public class PaymentReceiverInteract {

  private final CreateWalletInteract createWalletInteract;

  public PaymentReceiverInteract(CreateWalletInteract createWalletInteract) {
    this.createWalletInteract = createWalletInteract;
  }

  public Single<Wallet> createWallet() {
    return createWalletInteract.create()
        .flatMap(wallet -> createWalletInteract.setDefaultWallet(wallet)
            .andThen(Single.just(wallet)));
  }
}
