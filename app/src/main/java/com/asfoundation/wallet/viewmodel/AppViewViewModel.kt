package com.asfoundation.wallet.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.network.eskills.download.FileDownloadManager
import com.appcoins.wallet.core.network.eskills.downloadmanager.AppDownloadStatus
import com.appcoins.wallet.core.network.eskills.packageinstaller.InstallStatus
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.asfoundation.wallet.home.usecases.GetEskillsGameDetailsUseCase
import com.asfoundation.wallet.wallet.home.app_view.usecases.InstallAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import rx.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AppViewViewModel
@Inject
constructor(
  private val getEskillsGameDetailsUseCase: GetEskillsGameDetailsUseCase,
  private val installAppUseCase: InstallAppUseCase,
  private val rxSchedulers: RxSchedulers,
) : ViewModel() {
  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

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
      0
    )
  )
  val progress = mutableStateOf(0)
  private val fileDownloader = mutableStateOf(FileDownloadManager())
  val finishedInstall = mutableStateOf(false)
  val installing = mutableStateOf(false)

  private fun FileDownloadManager(): FileDownloadManager? {
    return null
  }

  fun fetchGameDetails(packageName: String) {
    compositeDisposable.add(
      getEskillsGameDetailsUseCase(packageName)
        .subscribeOn(rxSchedulers.io)
        .subscribe(
          { details -> gameDetails.value = details },
          { e -> e.printStackTrace() }
        )
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
              if (status.downloadProgress.totalFileBytes.toInt() == 0) {
                progress.value = 0
              } else {
                progress.value =
                  (((status.downloadProgress.downloadedBytes).toDouble() / (status.downloadProgress.totalFileBytes)) * 100).toInt()
              }
              if (status.downloadState.equals(AppDownloadStatus.AppDownloadState.COMPLETED)) {
                finished = true
                progress.value =
                  ((status.downloadProgress.downloadedBytes) / (status.downloadProgress.totalFileBytes) * 100).toInt()
                installAppUseCase.installApk(gameDetails.value)
                  .subscribe { callback ->
                    callback.installerInstallStatus
                      .subscribe { installStatus ->
                        if (installStatus.status.equals(InstallStatus.Status.INSTALLING)) {
                          installing.value = true
                          finishedInstall.value = false
                        }
                        if (installStatus.status.equals(InstallStatus.Status.SUCCESS)) {
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
    fileDownloader.value?.let {
      installAppUseCase.cancelDownload(it)
        .subscribeOn(Schedulers.io())
        .subscribe { downloader ->
          fileDownloader.value = downloader as FileDownloadManager?
        }
    }
  }

  fun pauseDownload() {
    fileDownloader.value?.let {
      installAppUseCase.pauseDownload(it)
        .subscribeOn(Schedulers.io())
        .subscribe { downloader ->
          fileDownloader.value = downloader as FileDownloadManager?
        }
    }
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

}