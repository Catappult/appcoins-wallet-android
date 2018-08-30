package com.asfoundation.wallet.poa;

import io.reactivex.Single;

public interface CountryCodeProvider {
  Single<String> getCountryCode();
}
