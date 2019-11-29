package com.asfoundation.wallet;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import retrofit2.HttpException;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by trinkes on 3/16/18.
 */

public class AirdropService {
  public static final String BASE_URL = "https://api.appstorefoundation.org/";
  private final Api api;
  private final Gson gson;
  private final Scheduler scheduler;

  public AirdropService(Api api, Gson gson, Scheduler scheduler) {
    this.api = api;
    this.gson = gson;
    this.scheduler = scheduler;
  }

  Single<AirDropResponse> requestAirdrop(String walletAddress, Integer chainId,
      String captchaAnswer) {
    return api.requestCoins(walletAddress, chainId, captchaAnswer)
        .subscribeOn(scheduler)
        .onErrorResumeNext(throwable -> {
          if (throwable instanceof HttpException) {
            return Single.just(gson.fromJson(((HttpException) throwable).response()
                .errorBody()
                .charStream(), AirDropResponse.class))
                .map(response -> {
                  if (((HttpException) throwable).code() == 406) {
                    return new AirDropResponse(Status.WRONG_CAPTCHA, response);
                  }
                  return new AirDropResponse(Status.FAIL, response);
                });
          } else {
            return Single.error(throwable);
          }
        });
  }

  Single<String> requestCaptcha(String walletAddress) {
    return api.requestCaptcha(walletAddress)
        .map(CaptchaResponse::getUrl);
  }

  public enum Status {
    OK, WRONG_CAPTCHA, FAIL
  }

  public interface Api {

    @GET("airdrop/{address}/funds") Single<AirDropResponse> requestCoins(
        @Path("address") String address, @Query("chain_id") int chainId,
        @Query("captcha_answer") String captchaAnswer);

    @GET("airdrop/captcha/{address}") Single<CaptchaResponse> requestCaptcha(
        @Path("address") String address);
  }

  public static class AirDropResponse {
    private final Status status;
    @SerializedName("txid_appc") private String appcoinsTransaction;
    @SerializedName("txid_eth") private String ethTransaction;
    @SerializedName("chain_id") private int chainId;
    @SerializedName("description") private String description;

    AirDropResponse(Status status, String appcoinsTransaction, String ethTransaction, int chainId,
        String description) {
      this.status = status;
      this.appcoinsTransaction = appcoinsTransaction;
      this.ethTransaction = ethTransaction;
      this.chainId = chainId;
      this.description = description;
    }

    private AirDropResponse(Status status, AirDropResponse airdropResponse) {
      this.status = status;
      appcoinsTransaction = airdropResponse.getAppcoinsTransaction();
      ethTransaction = airdropResponse.getEthTransaction();
      chainId = airdropResponse.getChainId();
      description = airdropResponse.getDescription();
    }

    public Status getStatus() {
      return status;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public int getChainId() {
      return chainId;
    }

    public void setChainId(int chainId) {
      this.chainId = chainId;
    }

    public String getEthTransaction() {
      return ethTransaction;
    }

    public void setEthTransaction(String ethTransaction) {
      this.ethTransaction = ethTransaction;
    }

    public String getAppcoinsTransaction() {
      return appcoinsTransaction;
    }

    public void setAppcoinsTransaction(String appcoinsTransaction) {
      this.appcoinsTransaction = appcoinsTransaction;
    }
  }

  public static class CaptchaResponse {
    @SerializedName("captcha_url") String url;

    public CaptchaResponse() {
    }

    public String getUrl() {
      return url;
    }
  }
}
