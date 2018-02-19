package com.asf.wallet.interact;

import com.asf.wallet.token.Erc20Token;
import io.reactivex.Single;

/**
 * Created by trinkes on 07/02/2018.
 */

public class BuildConfigDefaultTokenProvider implements DefaultTokenProvider {
  @Override public Single<String> getDefaultToken() {
    return Single.just(Erc20Token.APPC.getSymbol());
  }
}
