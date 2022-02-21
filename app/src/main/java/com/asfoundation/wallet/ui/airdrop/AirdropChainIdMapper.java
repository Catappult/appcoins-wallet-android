package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.entity.NetworkInfo;
import io.reactivex.Single;
import javax.inject.Inject;

public class AirdropChainIdMapper {
  private final NetworkInfo defaultNetwork;

  public @Inject AirdropChainIdMapper(NetworkInfo defaultNetwork) {
    this.defaultNetwork = defaultNetwork;
  }

  public Single<Integer> getAirdropChainId() {
    return Single.just(map(defaultNetwork));
  }

  private Integer map(NetworkInfo networkInfo) {
    if (networkInfo.chainId == 1) {
      return 1;
    }
    return 3;
  }
}
