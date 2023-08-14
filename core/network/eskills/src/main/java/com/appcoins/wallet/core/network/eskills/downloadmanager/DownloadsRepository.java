package com.appcoins.wallet.core.network.eskills.downloadmanager;

import com.appcoins.wallet.core.network.eskills.downloadmanager.database.room.RoomDownload;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by filipegoncalves on 8/21/18.
 */

public class DownloadsRepository {

  private final DownloadPersistence downloadPersistence;

  public DownloadsRepository(DownloadPersistence downloadPersistence) {
    this.downloadPersistence = downloadPersistence;
  }

  public Completable save(RoomDownload download) {
    return downloadPersistence.save(download);
  }

  public Completable remove(String md5) {
    return downloadPersistence.delete(md5);
  }

  public Completable remove(String packageName, int versionCode) {
    return downloadPersistence.delete(packageName, versionCode);
  }

  public Single<RoomDownload> getDownloadAsSingle(String md5) {
    return downloadPersistence.getAsSingle(md5);
  }

  public Observable<RoomDownload> getDownloadAsObservable(String md5) {
    return downloadPersistence.getAsObservable(md5);
  }

  public Observable<List<RoomDownload>> getDownloadsInProgress() {
    return downloadPersistence.getRunningDownloads();
  }

  public Observable<List<RoomDownload>> getInQueueDownloads() {
    return downloadPersistence.getInQueueSortedDownloads();
  }

  public Observable<List<RoomDownload>> getAllDownloads() {
    return downloadPersistence.getAll();
  }

  public Observable<List<RoomDownload>> getWaitingToMoveFilesDownloads() {
    return downloadPersistence.getUnmovedFilesDownloads();
  }

  public Observable<List<RoomDownload>> getDownloadListByMd5(String md5) {
    return downloadPersistence.getAsList(md5);
  }

  public Observable<List<RoomDownload>> getCurrentActiveDownloads() {
    return downloadPersistence.getRunningDownloads();
  }

  public Observable<List<RoomDownload>> getInProgressDownloadsList() {
    return downloadPersistence.getRunningDownloads()
        .flatMap(downloads -> Observable.fromIterable(downloads)
            .filter(download -> download.getOverallDownloadStatus() == RoomDownload.PROGRESS
                || download.getOverallDownloadStatus() == (RoomDownload.PENDING))
            .toList()
            .toObservable());
  }

  public Observable<List<RoomDownload>> getOutOfSpaceDownloads() {
    return downloadPersistence.getOutOfSpaceDownloads();
  }
}
