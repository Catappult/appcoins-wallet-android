package com.asfoundation.wallet.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.network.eskills.download.FileDownloadManager
import com.appcoins.wallet.core.network.eskills.downloadmanager.AppDownloadStatus
import com.appcoins.wallet.core.network.eskills.packageinstaller.InstallStatus
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.asfoundation.wallet.home.usecases.GetGameDetailsUseCase
import com.asfoundation.wallet.wallet.home.app_view.usecases.InstallAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel
@Inject
constructor(
  private val getGameDetailsUseCase: GetGameDetailsUseCase,
  private val installAppUseCase: InstallAppUseCase,
  private val rxSchedulers: RxSchedulers,
) : ViewModel() {

  val gameDetails = mutableStateOf(
    GameDetailsData(
    "",
    "",
    "",
    "",
    "",
    listOf(),
    0.0,
    0,
    0,
    "",
    "",
    0)
  )
  val progress = mutableStateOf(0)
  val fileDownloader = mutableStateOf(FileDownloadManager())
  val finishedInstall = mutableStateOf(false)
  val installing = mutableStateOf(false)

  private fun FileDownloadManager(): FileDownloadManager? {
    return null
  }

  init {

  }

  fun fetchGameDetails(packageName: String) {
    getGameDetailsUseCase(packageName)
      .subscribeOn(rxSchedulers.io)
      .subscribe(
        { details -> gameDetails.value = details },
        { e -> e.printStackTrace() }
      )

  }

  fun installApp() {
    installAppUseCase(gameDetails.value)
      ?.subscribeOn(Schedulers.io())
      ?.subscribe { downloader ->
        fileDownloader.value = downloader as FileDownloadManager?
        var finished = false
        while (
          !finished
        ) {
          finished = true
          downloader.observeFileDownloadProgress()
            .subscribe() { status ->
              if(status.downloadProgress.totalFileBytes.toInt() == 0) {
                Log.d("Total Bytes Zero ? ", "True")
                progress.value = 0
              } else {
                Log.d("Progress Value: ", progress.value.toString()+"%")
                Log.d("So Far Bytes: ", status.downloadProgress.downloadedBytes.toString()+"%")
                Log.d("Total Bytes: ", status.downloadProgress.totalFileBytes.toString()+"%")
                Log.d("Percentagem: ", (((status.downloadProgress.downloadedBytes) / (status.downloadProgress.totalFileBytes))*100).toInt().toString()+"%")
                progress.value =
                  (((status.downloadProgress.downloadedBytes).toDouble() / (status.downloadProgress.totalFileBytes))*100).toInt()

              }
              if(status.downloadState.equals(AppDownloadStatus.AppDownloadState.COMPLETED)){
                finished = true
                progress.value =
                  ((status.downloadProgress.downloadedBytes) / (status.downloadProgress.totalFileBytes) * 100).toInt()
                installAppUseCase.installApk(gameDetails.value)
                  .subscribe { callback ->
                    callback.installerInstallStatus
                      .subscribe { installStatus ->
                        if (installStatus.status.equals(InstallStatus.Status.INSTALLING)) {
                          Log.d("Install Progress", "Installing")
                          installing.value = true
                          finishedInstall.value = false
                        }
                        if (installStatus.status.equals(InstallStatus.Status.SUCCESS)) {
                          Log.d("Install Progress", "Finished")
                          finishedInstall.value = true
                          installing.value = false
                          cancelDownload()
                        }
                      }
                  }
              }
            }

        }


      }
  }

  fun cancelDownload() {
    Log.d("App View Model", "Enters cancel download")
    fileDownloader.value?.let {
      installAppUseCase.cancelDownload(it)
        .subscribeOn(Schedulers.io())
        .subscribe { downloader ->
          fileDownloader.value = downloader as FileDownloadManager?
        }
    }
  }

  fun pauseDownoad() {
    Log.d("App View Model", "Enters pause download")
    fileDownloader.value?.let {
      installAppUseCase.pauseDownload(it)
        .subscribeOn(Schedulers.io())
        .subscribe { downloader ->
          fileDownloader.value = downloader as FileDownloadManager?
        }
    }
  }

}