package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class FindDefaultNetworkInteract {

  private final EthereumNetworkRepositoryType ethereumNetworkRepository;
  private final Scheduler scheduler;

  public FindDefaultNetworkInteract(EthereumNetworkRepositoryType ethereumNetworkRepository,
      Scheduler scheduler) {
    this.ethereumNetworkRepository = ethereumNetworkRepository;
    this.scheduler = scheduler;
  }

  public Single<NetworkInfo> find() {
    return Single.just(ethereumNetworkRepository.getDefaultNetwork())
        .observeOn(scheduler);
  }
}
