package com.asfoundation.wallet.ui;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.iab.raiden.Raiden;
import com.bds.microraidenj.ws.ChannelHistoryResponse;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;

public class MicroRaidenInteractor {
  private final Raiden raiden;

  public MicroRaidenInteractor(Raiden raiden) {
    this.raiden = raiden;
  }

  public Completable closeChannel(String fromAddress) {
        return raiden.closeChannel(fromAddress);
  }

  public Single<List<ChannelHistoryResponse.MicroTransaction>> listTransactions(Wallet wallet) {
    return raiden.fetchTransactions(wallet.address);
  }

}
