package com.appcoins.wallet.core.network.eskills.downloadmanager;

import com.appcoins.wallet.core.network.eskills.room.RoomDownload;

public interface DownloadAnalytics {

  void onDownloadComplete(String md5, String packageName, int versionCode);

  void onError(String packageName, int versionCode, String md5, Throwable throwable);

  void startProgress(RoomDownload download);
}
