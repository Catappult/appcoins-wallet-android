package com.appcoins.wallet.core.network.eskills.downloadmanager;

public interface DownloadCompleteAnalytics {

  void onDownloadComplete(String md5, String packageName, int versionCode);
}
