package com.appcoins.wallet.core.network.eskills.downloadmanager;

import android.util.Log;
import androidx.annotation.VisibleForTesting;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.HashMap;
import io.reactivex.disposables.Disposable;
import io.reactivex.Flowable;

/**
 * Created by filipegoncalves on 7/27/18.
 */

public class AppDownloadManager implements AppDownloader {

  private static final String TAG = "AppDownloadManager";
  private final DownloadApp app;
  private RetryFileDownloaderProvider fileDownloaderProvider;
  private HashMap<String, RetryFileDownloader> fileDownloaderPersistence;
  private PublishSubject<FileDownloadCallback> fileDownloadSubject;
  private AppDownloadStatus appDownloadStatus;
  private Disposable subscribe;
  private DownloadAnalytics downloadAnalytics;

  public AppDownloadManager(RetryFileDownloaderProvider fileDownloaderProvider, DownloadApp app,
      HashMap<String, RetryFileDownloader> fileDownloaderPersistence,
      DownloadAnalytics downloadAnalytics) {
    this.fileDownloaderProvider = fileDownloaderProvider;
    this.app = app;
    this.fileDownloaderPersistence = fileDownloaderPersistence;
    this.downloadAnalytics = downloadAnalytics;
    fileDownloadSubject = PublishSubject.create();
    appDownloadStatus = new AppDownloadStatus(app.getMd5(), new ArrayList<>(),
        AppDownloadStatus.AppDownloadState.PENDING, app.getSize());
  }

  @Override public void startAppDownload() {
    subscribe = Flowable.fromIterable(app.getDownloadFiles())
        .flatMap(downloadAppFile -> startFileDownload(downloadAppFile, app.getAttributionId()))
        .subscribe(__ -> {
        }, Throwable::printStackTrace);
  }

  @Override public Completable pauseAppDownload() {
    return Flowable.fromIterable(app.getDownloadFiles())
        .flatMap(downloadAppFile -> getFileDownloader(downloadAppFile.getMainDownloadPath()))
        .filter(retryFileDownloader -> retryFileDownloader != null)
        .flatMapCompletable(fileDownloader -> fileDownloader.pauseDownload()
            .onErrorComplete());
  }

  @Override public Completable removeAppDownload() {
    return Flowable.fromIterable(app.getDownloadFiles())
        .flatMap(downloadAppFile -> getFileDownloader(downloadAppFile.getMainDownloadPath()))
        .flatMapCompletable(fileDownloader -> fileDownloader.removeDownloadFile()
            .onErrorComplete());
  }

  @Override public Flowable<AppDownloadStatus> observeDownloadProgress() {
    return observeFileDownload().flatMap(fileDownloadCallback -> {
      setAppDownloadStatus(fileDownloadCallback);
      return Flowable.just(appDownloadStatus);
    })
        .doOnError(throwable -> throwable.printStackTrace())
        .map(__ -> appDownloadStatus);
  }

  public void stop() {
    if (subscribe != null && !subscribe.isDisposed()) {
      subscribe.dispose();
      fileDownloadSubject = null;
      fileDownloaderPersistence.clear();
      fileDownloaderPersistence = null;
    }
  }

  private Flowable<FileDownloadCallback> startFileDownload(DownloadAppFile downloadAppFile,
      String attributionId) {
    return Flowable.just(
        fileDownloaderProvider.createRetryFileDownloader(downloadAppFile.getDownloadMd5(),
            downloadAppFile.getMainDownloadPath(), downloadAppFile.getFileType(),
            downloadAppFile.getPackageName(), downloadAppFile.getVersionCode(),
            downloadAppFile.getFileName(), PublishSubject.create(),
            downloadAppFile.getAlternativeDownloadPath(), attributionId))
        .doOnNext(
            fileDownloader -> fileDownloaderPersistence.put(downloadAppFile.getMainDownloadPath(),
                fileDownloader))
        .doOnNext(__ -> Log
            .d(TAG, "Starting app file download " + downloadAppFile.getFileName()))
        .doOnNext(fileDownloader -> fileDownloader.startFileDownload())
        .flatMap(fileDownloader -> handleFileDownloadProgress(fileDownloader))
        .doOnError(Throwable::printStackTrace);
  }

  private Flowable<FileDownloadCallback> observeFileDownload() {
    return fileDownloadSubject.toFlowable(BackpressureStrategy.LATEST);
  }

  private void setAppDownloadStatus(FileDownloadCallback fileDownloadCallback) {
    appDownloadStatus.setFileDownloadCallback(fileDownloadCallback);
  }

  private Flowable<FileDownloadCallback> handleFileDownloadProgress(
      RetryFileDownloader fileDownloader) {
    return fileDownloader.observeFileDownloadProgress()
        .doOnNext(fileDownloadCallback -> fileDownloadSubject.onNext(fileDownloadCallback))
        .doOnNext(fileDownloadCallback -> {
          if (fileDownloadCallback.getDownloadState() != null) {
            switch (fileDownloadCallback.getDownloadState()) {
              case COMPLETED:
                handleCompletedFileDownload(fileDownloader);
                break;
              case ERROR_FILE_NOT_FOUND:
              case ERROR:
              case ERROR_NOT_ENOUGH_SPACE:
                handleErrorFileDownload();
                if (fileDownloadCallback.hasError()) {
                  downloadAnalytics.onError(app.getPackageName(), app.getVersionCode(),
                      app.getMd5(), fileDownloadCallback.getError());
                }
                break;
            }
          }
        });
  }

  private void handleErrorFileDownload() {
    for (RetryFileDownloader retryFileDownloader : fileDownloaderPersistence.values()) {
      retryFileDownloader.stopFailedDownload();
    }
  }

  private void handleCompletedFileDownload(RetryFileDownloader fileDownloader) {
    fileDownloader.stop();
  }

  @VisibleForTesting
  public Flowable<RetryFileDownloader> getFileDownloader(String mainDownloadPath) {
    return Flowable.just(fileDownloaderPersistence.get(mainDownloadPath));
  }
}
