package com.appcoins.wallet.core.network.eskills.downloadmanager;


import com.appcoins.wallet.core.network.eskills.downloadmanager.utils.RoomDownload;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;


/**
 * Created by filipegoncalves on 7/27/18.
 */

public interface DownloadManager {

  void start();

  void stop();

  Completable startDownload(RoomDownload download);

  Flowable<RoomDownload> getDownloadAsObservable(String md5);

  Single<RoomDownload> getDownloadAsSingle(String md5);

  Flowable<RoomDownload> getDownloadsByMd5(String md5);

  Flowable<List<RoomDownload>> getDownloadsList();

  Flowable<RoomDownload> getCurrentInProgressDownload();

  Flowable<List<RoomDownload>> getCurrentActiveDownloads();

  Completable pauseAllDownloads();

  Completable pauseDownload(String md5);

  Completable removeDownload(String md5);

  Completable invalidateDatabase();
}
