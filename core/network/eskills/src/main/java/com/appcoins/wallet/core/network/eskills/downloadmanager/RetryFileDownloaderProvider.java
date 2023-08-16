package com.appcoins.wallet.core.network.eskills.downloadmanager;

import rx.subjects.PublishSubject;

public interface RetryFileDownloaderProvider {

  RetryFileDownloader createRetryFileDownloader(String md5, String mainDownloadPath, int fileType,
      String packageName, int versionCode, String fileName,
      PublishSubject<FileDownloadCallback> fileDownloadCallback, String alternativeDownloadPath,
      String attributionId);
}
