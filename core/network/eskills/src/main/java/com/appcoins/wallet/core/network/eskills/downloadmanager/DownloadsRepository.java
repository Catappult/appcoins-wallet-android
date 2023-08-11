package com.appcoins.wallet.core.network.eskills.downloadmanager;


import com.appcoins.wallet.core.network.eskills.downloadmanager.utils.RoomDownload;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;
import org.reactivestreams.Publisher;

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

  public Flowable<RoomDownload> getDownloadAsObservable(String md5) {
    return downloadPersistence.getAsObservable(md5);
  }

  public Flowable<List<RoomDownload>> getDownloadsInProgress() {
    return downloadPersistence.getRunningDownloads();
  }

  public Flowable<List<RoomDownload>> getInQueueDownloads() {
    return downloadPersistence.getInQueueSortedDownloads();
  }

  public Flowable<List<RoomDownload>> getAllDownloads() {
    return downloadPersistence.getAll();
  }

  public Flowable<List<RoomDownload>> getWaitingToMoveFilesDownloads() {
    return downloadPersistence.getUnmovedFilesDownloads();
  }

  public Flowable<List<RoomDownload>> getDownloadListByMd5(String md5) {
    return downloadPersistence.getAsList(md5);
  }

  public Flowable<List<RoomDownload>> getCurrentActiveDownloads() {
    return downloadPersistence.getRunningDownloads();
  }

  public Flowable<List<RoomDownload>> getInProgressDownloadsList() {
    return downloadPersistence.getRunningDownloads()
        .flatMap(downloads -> (Publisher<? extends List<RoomDownload>>) Flowable.fromIterable(downloads)
            .filter(download -> download.getOverallDownloadStatus() == RoomDownload.PROGRESS
                || download.getOverallDownloadStatus() == (RoomDownload.PENDING))
            .toList());
  }

  public Flowable<List<RoomDownload>> getOutOfSpaceDownloads() {
    return downloadPersistence.getOutOfSpaceDownloads();
  }
}
