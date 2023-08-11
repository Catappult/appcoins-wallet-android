package com.appcoins.wallet.core.network.eskills.downloadmanager;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Created by filipegoncalves on 7/27/18.
 */

public interface AppDownloader {

  void startAppDownload();

  Completable pauseAppDownload();

  Completable removeAppDownload();

  Flowable<AppDownloadStatus> observeDownloadProgress();

  void stop();
}
