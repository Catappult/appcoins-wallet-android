package com.asfoundation.contractproxy.proxy;

import io.reactivex.Single;

public interface WalletAddressProvider {
  Single<String> get();
}
