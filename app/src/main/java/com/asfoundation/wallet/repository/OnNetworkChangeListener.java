package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;

public interface OnNetworkChangeListener {
  void onNetworkChanged(NetworkInfo networkInfo);
}
