package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Single;

public class PaymentReceiverInteract {

  private final CreateWalletInteract createWalletInteract;
  private final AddTokenInteract addTokenInteract;
  private final DefaultTokenProvider defaultTokenProvider;

  public PaymentReceiverInteract(CreateWalletInteract createWalletInteract,
      AddTokenInteract addTokenInteract, DefaultTokenProvider defaultTokenProvider) {
    this.createWalletInteract = createWalletInteract;
    this.addTokenInteract = addTokenInteract;
    this.defaultTokenProvider = defaultTokenProvider;
  }

  public Single<Wallet> createWallet() {
    return createWalletInteract.create()
        .flatMap(wallet -> createWalletInteract.setDefaultWallet(wallet)
            .andThen(defaultTokenProvider.getDefaultToken())
            .flatMapCompletable(
                token -> addTokenInteract.add(token.address, token.symbol, token.decimals))
            .andThen(Single.just(wallet)));
  }
}
