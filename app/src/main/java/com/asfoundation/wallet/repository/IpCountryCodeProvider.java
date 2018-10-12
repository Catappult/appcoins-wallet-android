package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.Single;
import retrofit2.http.GET;

public class IpCountryCodeProvider implements CountryCodeProvider {
  public static String ENDPOINT = com.asf.wallet.BuildConfig.BACKEND_HOST;
  private final IpApi ipApi;

  public IpCountryCodeProvider(IpApi ipApi) {
    this.ipApi = ipApi;
  }

  @Override public Single<String> getCountryCode() {
    return ipApi.myIp()
        .map(IpResponse::getCountryCode);
  }

  public interface IpApi {
    @GET("exchange/countrycode") Single<IpResponse> myIp();
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
