package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FindDefaultNetworkInteract {

  private final EthereumNetworkRepositoryType ethereumNetworkRepository;

  public FindDefaultNetworkInteract(EthereumNetworkRepositoryType ethereumNetworkRepository) {
    this.ethereumNetworkRepository = ethereumNetworkRepository;
  }

  public Single<NetworkInfo> find() {
    return Single.just(ethereumNetworkRepository.getDefaultNetwork())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
