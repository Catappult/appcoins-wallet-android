package com.asf.wallet.interact;

import com.asf.wallet.entity.NetworkInfo;
import com.asf.wallet.repository.EthereumNetworkRepositoryType;
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
