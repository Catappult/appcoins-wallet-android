package com.wallet.crypto.trustapp.interact;

import io.reactivex.Single;

/**
 * Created by trinkes on 07/02/2018.
 */

public class BuildConfigDefaultTokenProvider implements DefaultTokenProvider {
    @Override
    public Single<String> getDefaultToken() {
        return Single.just("APPC");
    }
}
