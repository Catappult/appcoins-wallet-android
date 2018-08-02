package com.asfoundation.wallet.ws.transactions;

import com.asfoundation.wallet.ws.WSFactory;
import com.asfoundation.wallet.ws.WebService;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import rx.Observable;

public class PatchTransaction extends WebService<PatchTransaction.Service, Object> {

  private final String uid;
  private String paykey;

  public PatchTransaction(String uid, String paykey) {
    super(Service.class, WSFactory.newOkHttpClient(), WSFactory.newJacksonConverterFactory(),
        CreateAdyenTransaction.BASE_HOST);

    this.uid = uid;
    this.paykey = paykey;
  }

  @Override protected Observable<Object> loadDataFromNetwork(Service service, boolean bypassCache) {
    return service.patchTransaction(uid, paykey);
  }

  public interface Service {
    //@GET("inapp/8.20180401/gateways/adyen/transactions/{uid}/authorization?wallet.address=0xBB83e699F1188bAAbEa820ce02995C97BD9b510F&wallet.signature=e619ae601013a0071dde91c31d67b14b3938a32c90f5c434c283fa4a2e15e75f27ceef30609156285b827bd936da6e099b89fd982553b719f74c86e828ee0a2800")
    @FormUrlEncoded
    @PATCH("http://api-dev.blockchainds.com/inapp/8.20180401/gateways/adyen/transactions/{uid}?wallet.address=0xBB83e699F1188bAAbEa820ce02995C97BD9b510F&wallet.signature=e619ae601013a0071dde91c31d67b14b3938a32c90f5c434c283fa4a2e15e75f27ceef30609156285b827bd936da6e099b89fd982553b719f74c86e828ee0a2800")
    Observable<Object> patchTransaction(@Path("uid") String uid, @Field("paykey") String paykey);
  }
}
