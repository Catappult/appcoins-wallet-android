package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.appcoins.wallet.core.network.backend.api.IpApi;
import com.appcoins.wallet.core.network.backend.model.IpResponse;
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProvider;
import io.reactivex.Single;
import it.czerwinski.android.hilt.annotations.BoundTo;
import javax.inject.Inject;

@BoundTo(supertype = CountryCodeProvider.class) public class IpCountryCodeProvider
    implements CountryCodeProvider {
  private final IpApi ipApi;

  public @Inject IpCountryCodeProvider(IpApi ipApi) {
    this.ipApi = ipApi;
  }

  @NonNull @Override public Single<String> getCountryCode() {
    return ipApi.myIp()
        .map(IpResponse::getCountryCode);
  }
}
