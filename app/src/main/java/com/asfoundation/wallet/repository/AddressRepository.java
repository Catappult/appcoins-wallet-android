package com.asfoundation.wallet.repository;

import com.asf.wallet.BuildConfig;

/**
 * Created by trinkes on 07/03/2018.
 */

public class AddressRepository {
  public String getOemAddress() {
    return BuildConfig.DEFAULT_OEM_ADREESS;
  }

  public String getStoreAddress() {
    return BuildConfig.DEFAULT_OEM_ADREESS;
  }
}
