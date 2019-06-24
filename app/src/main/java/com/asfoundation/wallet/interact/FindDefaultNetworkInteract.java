package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.NetworkInfo;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class FindDefaultNetworkInteract {

  private final NetworkInfo defaultNetwork;
  private final Scheduler scheduler;

  public FindDefaultNetworkInteract(NetworkInfo defaultNetwork, Scheduler scheduler) {
    this.defaultNetwork = defaultNetwork;
    this.scheduler = scheduler;
  }

  public Single<NetworkInfo> find() {
    return Single.just(defaultNetwork)
        .observeOn(scheduler);
  }
}
