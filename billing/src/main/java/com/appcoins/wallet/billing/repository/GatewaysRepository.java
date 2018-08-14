package com.appcoins.wallet.billing.repository;

import com.appcoins.wallet.billing.repository.entity.TransactionStatus;
import com.appcoins.wallet.billing.repository.entity.authorization.Authorization;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class GatewaysRepository {

  public static final String BASE_HOST_DEV =
      "http://api-dev.blockchainds.com/inapp/8.20180401/gateways/";
  public static final String BASE_HOST_PROD =
      "http://api.blockchainds.com/inapp/8.20180401/gateways/";

  private final BdsGatewaysApi bdsGatewaysApi;

  public GatewaysRepository(BdsGatewaysApi bdsGatewaysApi) {
    this.bdsGatewaysApi = bdsGatewaysApi;
  }

  public Completable patchTransaction(String uid, String walletAddress, String walletSignature,
      String paykey) {
    return bdsGatewaysApi.patchTransaction(uid, walletAddress, walletSignature, paykey)
        .ignoreElements();
  }

  public Single<Authorization> getSessionKey(String uid, String walletAddress,
      String walletSignature) {
    return bdsGatewaysApi.getSessionKey(uid, walletAddress, walletSignature)
        .singleOrError();
  }

  public Single<TransactionStatus> createAdyenTransaction(String walletAddress,
      String walletSignature, String token, String payload, String packageName, String productName,
      String walletDeveloper, String walletStore) {
    return bdsGatewaysApi.createAdyenTransaction(walletAddress, walletSignature, payload,
        packageName, productName, walletDeveloper, token, walletStore)
        .singleOrError();
  }

  public interface BdsGatewaysApi {

    @FormUrlEncoded @PATCH("adyen/transactions/{uid}") Observable<Object> patchTransaction(
        @Path("uid") String uid, @Query("wallet.address") String walletAddress,
        @Query("wallet.signature") String walletSignature, @Field("paykey") String paykey);

    @GET("adyen/transactions/{uid}/authorization") Observable<Authorization> getSessionKey(
        @Path("uid") String uid, @Query("wallet.address") String walletAddress,
        @Query("wallet.signature") String walletSignature);

    @FormUrlEncoded @POST("adyen/transactions")
    Observable<TransactionStatus> createAdyenTransaction(
        @Query("wallet.address") String walletAddress,
        @Query("wallet.signature") String walletSignature, @Field("payload") String payload,
        @Field("package.name") String packageName, @Field("product.name") String productName,
        @Field("wallets.developer") String walletsDeveloper, @Field("token") String token,
        @Field("wallets.store") String walletsStore);
  }
}
