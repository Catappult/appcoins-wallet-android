package com.wallet.crypto.trustapp.interact;

import io.reactivex.Single;

/**
 * Created by trinkes on 07/02/2018.
 */

interface DefaultTokenProvider {
  Single<String> getDefaultToken();
}
