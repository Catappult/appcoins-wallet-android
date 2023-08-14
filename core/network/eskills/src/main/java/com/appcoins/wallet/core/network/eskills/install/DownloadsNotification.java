package com.appcoins.wallet.core.network.eskills.install;

/**
 * Created by tiagopedrinho on 09/10/2018.
 */

public interface DownloadsNotification {

  void setupProgressNotification(String md5, String appName, int progress, boolean isIndeterminate);

  void removeProgressNotificationAndStop();
}
