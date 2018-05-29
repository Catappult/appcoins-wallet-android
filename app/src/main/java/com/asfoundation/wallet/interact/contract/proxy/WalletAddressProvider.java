package com.asfoundation.wallet.interact.contract.proxy;

import io.reactivex.Single;

public interface WalletAddressProvider {
  Single<String> get();
}
