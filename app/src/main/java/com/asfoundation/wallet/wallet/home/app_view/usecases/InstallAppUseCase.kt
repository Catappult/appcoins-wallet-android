package com.asfoundation.wallet.wallet.home.app_view.usecases

import android.os.Environment
import com.appcoins.wallet.core.network.eskills.download.FileDownloadManagerProvider
import com.appcoins.wallet.core.network.eskills.downloadmanager.AppDownloadStatus
import com.appcoins.wallet.core.network.eskills.downloadmanager.FileDownloader
import com.appcoins.wallet.core.network.eskills.downloadmanager.FileDownloaderProvider
import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstall
import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstaller

import rx.Observable
import rx.subjects.PublishSubject
import java.io.File
import javax.inject.Inject


class InstallAppUseCase @Inject constructor(
  private val fileDownloadManagerProvider: FileDownloadManagerProvider,
  private val appInstaller: AppInstaller
) {
  operator fun invoke(gamePackage: String) {
    val fileDownloader:FileDownloader = fileDownloadManagerProvider.createFileDownloader(
      "c3a4c5da0ce2fd44824e747aa277be5b",
      "https://pool.apk.aptoide.com/catappult/com-appcoins-eskills2048-37-65157799-c3a4c5da0ce2fd44824e747aa277be5b.apk",
      0,
      "com.appcoins.eskills2048",
      37,
      "c3a4c5da0ce2fd44824e747aa277be5b",
      PublishSubject.create(),
      "1"
    )

    fileDownloader.startFileDownload()

    while(
      !fileDownloader.observeFileDownloadProgress()
        .flatMap { status ->
          Observable.just(status.downloadState.equals(AppDownloadStatus.AppDownloadState.COMPLETED))
        }
        .toBlocking().first()
    ) { }

    val appInstall = AppInstall(
      "com.appcoins.eskills2048",
      File(
        Environment.getExternalStorageDirectory()
          .absolutePath + "/.wallet/c3a4c5da0ce2fd44824e747aa277be5b"))

    appInstaller.install(appInstall)

  }




}