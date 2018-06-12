package com.asfoundation.wallet.ui;

import com.asfoundation.wallet.ui.iab.raiden.Raiden;
import io.reactivex.Completable;

public class MicroRaidenInteractor {
  private final Raiden raiden;

  public MicroRaidenInteractor(Raiden raiden) {
    this.raiden = raiden;
  }

  public Completable closeChannel(String fromAddress) {
        return raiden.closeChannel(fromAddress);
  }

}
