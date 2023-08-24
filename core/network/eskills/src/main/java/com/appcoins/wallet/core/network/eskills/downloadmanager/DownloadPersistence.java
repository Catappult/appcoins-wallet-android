package com.appcoins.wallet.core.network.eskills.downloadmanager;

import com.appcoins.wallet.core.network.eskills.room.RoomDownload;
import java.util.List;
import rx.Completable;
import rx.Observable;
import rx.Single;

public interface DownloadPersistence {

  Observable<List<RoomDownload>> getAll();

  Single<RoomDownload> getAsSingle(String md5);

  Observable<RoomDownload> getAsObservable(String md5);

  Completable delete(String md5);

  Completable save(RoomDownload download);

  Observable<List<RoomDownload>> getRunningDownloads();

  Observable<List<RoomDownload>> getInQueueSortedDownloads();

  Observable<List<RoomDownload>> getAsList(String md5);

  Observable<List<RoomDownload>> getUnmovedFilesDownloads();

  Completable delete(String packageName, int versionCode);

  Observable<List<RoomDownload>> getOutOfSpaceDownloads();
}