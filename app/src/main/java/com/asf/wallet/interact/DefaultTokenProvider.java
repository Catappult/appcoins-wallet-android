package com.asf.wallet.interact;

import io.reactivex.Single;

/**
 * Created by trinkes on 07/02/2018.
 */

interface DefaultTokenProvider {
  Single<String> getDefaultToken();
}
