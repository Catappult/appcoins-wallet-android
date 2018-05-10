package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import io.reactivex.Single;

public class AirdropChainIdMapper {
  private final FindDefaultNetworkInteract defaultNetworkInteract;

  public AirdropChainIdMapper(FindDefaultNetworkInteract defaultNetworkInteract) {
    this.defaultNetworkInteract = defaultNetworkInteract;
  }

  public Single<Integer> getAirdropChainId() {
    return defaultNetworkInteract.find()
        .map(this::map);
  }

  private Integer map(NetworkInfo networkInfo) {
    switch (networkInfo.chainId) {
      case 1:
        return 1;
      default:
        return 3;
    }
  }
}
