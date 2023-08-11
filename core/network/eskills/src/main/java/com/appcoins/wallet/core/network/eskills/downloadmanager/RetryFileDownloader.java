package com.appcoins.wallet.core.network.eskills.downloadmanager;

import io.reactivex.Completable;
import io.reactivex.Flowable;


public interface RetryFileDownloader {

  void startFileDownload();

  Completable pauseDownload();

  Completable removeDownloadFile();

  Flowable<FileDownloadCallback> observeFileDownloadProgress();

  void stop();

  void stopFailedDownload();
}
