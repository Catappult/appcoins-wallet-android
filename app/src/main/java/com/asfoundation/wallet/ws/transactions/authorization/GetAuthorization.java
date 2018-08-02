package com.asfoundation.wallet.ws.transactions.authorization;

import com.asfoundation.wallet.ws.WSFactory;
import com.asfoundation.wallet.ws.WebService;
import com.asfoundation.wallet.ws.transactions.CreateAdyenTransaction;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public class GetAuthorization extends WebService<GetAuthorization.Service, Authorization> {

  private final String uid;

  public GetAuthorization(String uid) {
    super(Service.class, WSFactory.newOkHttpClient(), WSFactory.newJacksonConverterFactory(),
        CreateAdyenTransaction.BASE_HOST);

    this.uid = uid;
  }

  @Override
  protected Observable<Authorization> loadDataFromNetwork(Service service, boolean bypassCache) {
    return service.getSessionKey(uid);
  }

  public interface Service {
    @GET("inapp/8.20180401/gateways/adyen/transactions/{uid}/authorization?wallet.address=0xBB83e699F1188bAAbEa820ce02995C97BD9b510F&wallet.signature=e619ae601013a0071dde91c31d67b14b3938a32c90f5c434c283fa4a2e15e75f27ceef30609156285b827bd936da6e099b89fd982553b719f74c86e828ee0a2800")
    Observable<Authorization> getSessionKey(@Path("uid") String uid);
  }
}
