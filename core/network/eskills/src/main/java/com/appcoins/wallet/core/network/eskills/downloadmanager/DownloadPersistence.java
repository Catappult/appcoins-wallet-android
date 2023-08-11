package com.appcoins.wallet.core.network.eskills.downloadmanager;


import com.appcoins.wallet.core.network.eskills.downloadmanager.utils.RoomDownload;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;


public interface DownloadPersistence {

  Flowable<List<RoomDownload>> getAll();

  Single<RoomDownload> getAsSingle(String md5);

  Flowable<RoomDownload> getAsObservable(String md5);

  Completable delete(String md5);

  Completable save(RoomDownload download);

  Flowable<List<RoomDownload>> getRunningDownloads();

  Flowable<List<RoomDownload>> getInQueueSortedDownloads();

  Flowable<List<RoomDownload>> getAsList(String md5);

  Flowable<List<RoomDownload>> getUnmovedFilesDownloads();

  Completable delete(String packageName, int versionCode);

  Flowable<List<RoomDownload>> getOutOfSpaceDownloads();
}