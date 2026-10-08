package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.appcoins.wallet.core.network.backend.api.CountryApi;
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProvider;
import io.reactivex.Single;
import javax.inject.Inject;

public class IpCountryCodeProvider
    implements CountryCodeProvider {
  private final CountryApi countryApi;

  public @Inject IpCountryCodeProvider(CountryApi countryApi) {
    this.countryApi = countryApi;
  }

  @NonNull @Override public Single<String> getCountryCode() {
    return countryApi.getCountryCode(null)
        .map(countryResponse -> countryResponse.getCountryCode() != null ? countryResponse.getCountryCode() : "");
  }
}
