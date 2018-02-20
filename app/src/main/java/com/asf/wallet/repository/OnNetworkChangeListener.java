package com.asf.wallet.repository;

import com.asf.wallet.entity.NetworkInfo;

public interface OnNetworkChangeListener {
  void onNetworkChanged(NetworkInfo networkInfo);
}
