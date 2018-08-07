package com.asfoundation.wallet.ws.transactions;

import com.asfoundation.wallet.ws.WSFactory;
import com.asfoundation.wallet.ws.WebService;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public class CreateAdyenTransaction
    extends WebService<CreateAdyenTransaction.Service, TransactionStatus> {

  public static final String BASE_HOST = "http://api-dev.blockchainds.com/";

  private final String token;

  public CreateAdyenTransaction(String token) {
    super(Service.class, WSFactory.newOkHttpClient(), WSFactory.newJacksonConverterFactory(),
        BASE_HOST);
    this.token = token;
  }

  @Override protected Observable<TransactionStatus> loadDataFromNetwork(Service service,
      boolean bypassCache) {

    String payload = "hello";
    String packageName = "com.appcoins.trivialdrivesample.test";
    String productName = "gas";
    String walletsDeveloper = "0xda99070eb09ab6ab7e49866c390b01d3bca9d516";
    String walletsStore = "0xd95c64c6eee9164539d679354f349779a04f57cb";

    return service.createAdyenTransaction(payload, packageName, productName, walletsDeveloper,
        token, walletsStore);
  }

  public interface Service {
    @FormUrlEncoded
    @POST("inapp/8.20180401/gateways/adyen/transactions?wallet.address=0xBB83e699F1188bAAbEa820ce02995C97BD9b510F&wallet.signature=e619ae601013a0071dde91c31d67b14b3938a32c90f5c434c283fa4a2e15e75f27ceef30609156285b827bd936da6e099b89fd982553b719f74c86e828ee0a2800")
    Observable<TransactionStatus> createAdyenTransaction(@Field("payload") String payload,
        @Field("package.name") String packageName, @Field("product.name") String productName,
        @Field("wallets.developer") String walletsDeveloper, @Field("token") String token,
        @Field("wallets.store") String walletsStore);
  }
}
