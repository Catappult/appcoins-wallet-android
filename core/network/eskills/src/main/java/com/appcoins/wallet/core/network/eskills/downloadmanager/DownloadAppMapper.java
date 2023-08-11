package com.appcoins.wallet.core.network.eskills.downloadmanager;

import cm.aptoide.pt.database.room.RoomDownload;
import java.util.List;

/**
 * Created by filipegoncalves on 9/12/18.
 */

public class DownloadAppMapper {

  private DownloadAppFileMapper downloadAppFileMapper;

  public DownloadAppMapper(DownloadAppFileMapper downloadAppFileMapper) {
    this.downloadAppFileMapper = downloadAppFileMapper;
  }

  public DownloadApp mapDownload(RoomDownload download) {
    List<DownloadAppFile> fileList =
        downloadAppFileMapper.mapFileToDownloadList(download.getFilesToDownload());
    return new DownloadApp(download.getPackageName(), download.getVersionCode(), fileList,
        download.getMd5(), download.getSize(), download.getAttributionId());
  }
}
