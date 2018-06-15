package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.TokenInfo;
import io.reactivex.Single;

/**
 * Created by trinkes on 07/02/2018.
 */

public interface DefaultTokenProvider {
  Single<TokenInfo> getDefaultToken();
}
