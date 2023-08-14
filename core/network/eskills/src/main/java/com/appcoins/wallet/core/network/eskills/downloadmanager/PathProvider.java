package com.appcoins.wallet.core.network.eskills.downloadmanager;

import com.appcoins.wallet.core.network.eskills.downloadmanager.database.room.RoomFileToDownload;

public interface PathProvider {

  String getFilePathFromFileType(RoomFileToDownload fileToDownload);
}
