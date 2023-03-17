package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.Single;
import it.czerwinski.android.hilt.annotations.BoundTo;
import javax.inject.Inject;
import retrofit2.http.GET;

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

  public interface IpApi {
    @GET("appc/countrycode") Single<IpResponse> myIp();
  }

  @JsonInclude(JsonInclude.Include.NON_NULL) public class IpResponse {

    @JsonProperty("countryCode") private String countryCode;

    public IpResponse() {
    }

    public String getCountryCode() {
      return countryCode;
    }

    public void setCountryCode(String countryCode) {
      this.countryCode = countryCode;
    }
  }
}
