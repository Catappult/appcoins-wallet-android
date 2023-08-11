package com.appcoins.wallet.core.network.eskills.downloadmanager;

import cm.aptoide.pt.database.room.RoomFileToDownload;

public interface PathProvider {

  String getFilePathFromFileType(RoomFileToDownload fileToDownload);
}
