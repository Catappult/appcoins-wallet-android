package com.appcoins.wallet.core.network.eskills.downloadmanager;

import rx.Observable;

/**
 * Created by trinkes on 9/13/16.
 */
public interface CacheManager {

  Observable<Long> cleanCache();
}
