package com.asfoundation.wallet.wallet.home.app_view.usecases

import android.os.Environment
import android.util.Log
import com.appcoins.wallet.core.network.eskills.download.FileDownloadManagerProvider
import com.appcoins.wallet.core.network.eskills.downloadmanager.FileDownloader
import com.appcoins.wallet.core.network.eskills.install.AppInstallerStatusReceiver
import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstall
import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstaller
import com.appcoins.wallet.ui.widgets.GameDetailsData
import rx.Single
import rx.subjects.PublishSubject
import java.io.File
import javax.inject.Inject


class InstallAppUseCase @Inject constructor(
  private val fileDownloadManagerProvider: FileDownloadManagerProvider,
  private val appInstaller: AppInstaller,
  private val installCallback: AppInstallerStatusReceiver
) {

  operator fun invoke(appDetails: GameDetailsData): Single<FileDownloader>? {
    Log.d("InstallAppUseCase", "Enters invoke")
    val fileDownloader = fileDownloadManagerProvider.createFileDownloader(
      appDetails.md5,
      appDetails.url,
      0,
      appDetails.gamePackage,
      37,
      appDetails.md5 + ".apk",
      PublishSubject.create(),
      "1"
    )
    Log.d("InstallAppUseCase", "about to start downloading")
    val fileReturn = fileDownloader.startFileDownload().andThen(Single.just(fileDownloader))

    return fileReturn

  }


  fun installApk(gameDetailsData: GameDetailsData): Single<AppInstallerStatusReceiver> {
    val file = File(
      Environment.getExternalStorageDirectory()
        .absolutePath + "/.aptoide/" + gameDetailsData.md5 + ".apk"
    )

    if (file.exists()) {
      Log.d("File", "File Exists")
    } else {
      Log.d("File", "File doesnt exists")
    }

    val appInstall = AppInstall(
      gameDetailsData.gamePackage,
      file
    )

    appInstaller.install(appInstall)

    return Single.just(installCallback)
  }

  fun cancelDownload(downloader: FileDownloader): Single<FileDownloader> {
    return downloader.removeDownloadFile().andThen(Single.just(downloader))
  }

  fun pauseDownload(downloader: FileDownloader): Single<FileDownloader> {
    return downloader.pauseDownload().andThen(Single.just(downloader))
  }


}