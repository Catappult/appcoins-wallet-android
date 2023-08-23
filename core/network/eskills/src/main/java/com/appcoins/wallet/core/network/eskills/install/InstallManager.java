/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 04/10/2016.
 */

package com.appcoins.wallet.core.network.eskills.install;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import cm.aptoide.pt.app.aptoideinstall.AptoideInstallManager;

import cm.aptoide.pt.install.InstallAppSizeValidator;


import com.appcoins.wallet.core.network.eskills.downloadmanager.AptoideDownloadManager;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadNotFoundException;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadsRepository;

import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstaller;
import com.appcoins.wallet.core.network.eskills.room.RoomDownload;
import com.appcoins.wallet.core.network.eskills.room.RoomInstalled;
import com.appcoins.wallet.core.network.eskills.utils.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Nullable;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by marcelobenites on 9/29/16.
 */

public class InstallManager {

  private static final String TAG = "InstallManager";
  private final AptoideDownloadManager aptoideDownloadManager;
  private final AppInstaller installer;
  private final SharedPreferences sharedPreferences;
  private final SharedPreferences securePreferences;
  private final PackageInstallerManager packageInstallerManager;
  private final DownloadsRepository downloadRepository;
  private final AptoideInstalledAppsRepository aptoideInstalledAppsRepository;


  private final AptoideInstallManager aptoideInstallManager;
  private final InstallAppSizeValidator installAppSizeValidator;


  private final CompositeSubscription dispatchInstallationsSubscription =
      new CompositeSubscription();

  public InstallManager(AptoideDownloadManager aptoideDownloadManager,
      AppInstaller installer,
      SharedPreferences sharedPreferences, SharedPreferences securePreferences,
      DownloadsRepository downloadRepository,
      AptoideInstalledAppsRepository aptoideInstalledAppsRepository,
      PackageInstallerManager packageInstallerManager,
      AptoideInstallManager aptoideInstallManager, InstallAppSizeValidator installAppSizeValidator) {
    this.aptoideDownloadManager = aptoideDownloadManager;
    this.installer = installer;

    this.downloadRepository = downloadRepository;
    this.aptoideInstalledAppsRepository = aptoideInstalledAppsRepository;
    this.sharedPreferences = sharedPreferences;
    this.securePreferences = securePreferences;
    this.packageInstallerManager = packageInstallerManager;

    this.aptoideInstallManager = aptoideInstallManager;
    this.installAppSizeValidator = installAppSizeValidator;
  }

  public void start() {
    aptoideDownloadManager.start();

  }


  public void stop() {
    aptoideDownloadManager.stop();

  }

  public Completable cancelInstall(String md5, String packageName, int versionCode) {
    return pauseInstall(md5).andThen(
        aptoideInstalledAppsRepository.remove(packageName, versionCode))
        .andThen(aptoideDownloadManager.removeDownload(md5))
        .doOnError(throwable -> throwable.printStackTrace());
  }

  public Completable pauseInstall(String md5) {
    return aptoideDownloadManager.pauseDownload(md5);
  }

  public Observable<List<Install>> getTimedOutInstallations() {
    return getInstallations().flatMap(installs -> Observable.from(installs)
        .filter(install -> install.getState()
            .equals(Install.InstallationStatus.INSTALLATION_TIMEOUT))
        .toList());
  }

  public Observable<List<Install>> getInstallations() {
    return Observable.combineLatest(aptoideDownloadManager.getDownloadsList(),
        aptoideInstalledAppsRepository.getAllInstalled(),
        aptoideInstalledAppsRepository.getAllInstalling(), this::createInstallList)
        .distinctUntilChanged();
  }

  private synchronized List<Install> createInstallList(List<RoomDownload> downloads,
      List<RoomInstalled> installedAppsList, List<RoomInstalled> installingAppList) {

    List<Install> installList = new ArrayList<>();
    for (RoomDownload download : downloads) {

      boolean isInstalling =
          isAppInstalling(installingAppList, download.getPackageName(), download.getVersionCode());
      int installStatus = RoomInstalled.STATUS_UNINSTALLED;
      if (isInstalling) {
        installStatus = RoomInstalled.STATUS_INSTALLING;
      }

      Install.InstallationType installationType = Install.InstallationType.INSTALL;

      for (RoomInstalled installed : installedAppsList) {
        if (download.getPackageName()
            .equals(installed.getPackageName())) {

          if (download.getVersionCode() == installed.getVersionCode()) {
            if (!isInstalling) {
              installStatus = installed.getStatus();
            }
            installationType = Install.InstallationType.INSTALLED;
          } else {

            if (installed.getVersionCode() > download.getVersionCode()) {
              installationType = Install.InstallationType.DOWNGRADE;
            } else {
              installationType = Install.InstallationType.UPDATE;
            }
          }
          break;
        }
      }
      installList.add(
          createInstall(download, download.getMd5(), download.getPackageName(),
              download.getVersionCode(), installationType));
    }
    return installList;
  }

  private boolean isAppInstalling(List<RoomInstalled> installingAppList, String packageName,
      int versionCode) {
    for (RoomInstalled installing : installingAppList) {
      if (packageName.equals(installing.getPackageName())
          && versionCode == installing.getVersionCode()) {
        return true;
      }
    }
    return false;
  }

  public Observable<Install> getCurrentInstallation() {
    return aptoideDownloadManager.getCurrentInProgressDownload()
        .filter(download -> download != null)
        .observeOn(Schedulers.io())
        .distinctUntilChanged(download -> download.getMd5())
        .flatMap(download -> getInstall(download.getMd5(), download.getPackageName(),
            download.getVersionCode()));
  }

  public Completable install(RoomDownload download) {
    boolean shouldForceDefaultInstall =
        sharedPreferences.getBoolean(ManagedKeys.ENFORCE_NATIVE_INSTALLER_KEY, false);
    return install(download, shouldForceDefaultInstall, false, true);
  }

  public Completable install(RoomDownload download, boolean shouldInstall) {
    boolean shouldForceDefaultInstall =
        sharedPreferences.getBoolean(ManagedKeys.ENFORCE_NATIVE_INSTALLER_KEY, false);
    return install(download, shouldForceDefaultInstall, false, shouldInstall);
  }

  private Completable defaultInstall(RoomDownload download) {
    return install(download, true, false, true);
  }

  public Completable splitInstall(RoomDownload download) {
    boolean shouldForceDefaultInstall =
        sharedPreferences.getBoolean(ManagedKeys.ENFORCE_NATIVE_INSTALLER_KEY, false);
    return install(download, shouldForceDefaultInstall, !shouldForceDefaultInstall, true);
  }

  private Completable install(RoomDownload download, boolean forceDefaultInstall,
      boolean forceSplitInstall, boolean shouldInstall) {
    return aptoideDownloadManager.getDownloadAsSingle(download.getMd5())
        .doOnError(Throwable::printStackTrace)
        .toObservable()
        .flatMap(storedDownload -> updateDownloadAction(download, storedDownload).andThen(
            Observable.just(storedDownload)))
        .retryWhen(errors -> createDownloadAndRetry(errors, download))
        .doOnNext(storedDownload -> aptoideInstallManager.addAptoideInstallCandidate(
            storedDownload.getPackageName()))
        .flatMap(storedDownload -> {
          if (storedDownload.getOverallDownloadStatus() == RoomDownload.ERROR) {
            storedDownload.setOverallDownloadStatus(RoomDownload.INVALID_STATUS);
            return downloadRepository.save(storedDownload)
                .andThen(Observable.just(storedDownload));
          }
          return Observable.just(storedDownload);
        })
        .flatMap(savedDownload -> startBackgroundInstallation(download.getMd5(), forceDefaultInstall,
            packageInstallerManager.shouldSetInstallerPackageName() || forceSplitInstall,
            shouldInstall))
        .toCompletable();
  }

  public Observable<Install> getInstall(String md5, String packageName, int versioncode) {
    return Observable.combineLatest(aptoideDownloadManager.getDownloadsByMd5(md5),
         getInstallationType(packageName, versioncode),
        (download, installationType) -> createInstall(download,
             md5, packageName, versioncode, installationType))
        .doOnNext(install -> Logger.getInstance()
            .d(TAG, install.toString()));
  }

  private Install createInstall(RoomDownload download,
      String md5, String packageName, int versioncode, Install.
      InstallationType installationType) {
    return new Install(mapInstallation(download),
        mapInstallationStatus(download), installationType,
        mapIndeterminateState(download), getSpeed(download), md5, packageName,
        versioncode, getVersionName(download),
        getAppName(download), getAppIcon(download),
        getDownloadSize(download));
  }

  private long getDownloadSize(RoomDownload download) {

      return download.getSize();

  }

  private String getVersionName(RoomDownload download) {

      return download.getVersionName();

  }

  private String getAppIcon(RoomDownload download) {
      return download.getIcon();

  }

  private String getAppName(RoomDownload download) {
      return download.getAppName();
  }

  private int getSpeed(RoomDownload download) {
    if (download != null) {
      return download.getDownloadSpeed();
    } else {
      return 0;
    }
  }

  private boolean mapIndeterminateState(RoomDownload download) {
    return mapIndeterminate(download);
  }

  private Install.InstallationStatus mapInstallationStatus(RoomDownload download) {

    return mapDownloadState(download);
  }

  private int mapInstallation(RoomDownload download) {
    int progress = 0;
    if (download != null) {
      progress = download.getOverallProgress();
      Logger.getInstance()
          .d(TAG, " download is not null "
              + progress
              + " state "
              + download.getOverallDownloadStatus());
    } else {
      Logger.getInstance()
          .d(TAG, " download is null");
    }
    return progress;
  }

  private boolean mapIndeterminate(RoomDownload download) {
    boolean isIndeterminate = false;
    if (download != null) {
      switch (download.getOverallDownloadStatus()) {
        case RoomDownload.IN_QUEUE:
        case RoomDownload.VERIFYING_FILE_INTEGRITY:
        case RoomDownload.WAITING_TO_MOVE_FILES:
          isIndeterminate = true;
          break;
        case RoomDownload.BLOCK_COMPLETE:
        case RoomDownload.COMPLETED:
        case RoomDownload.CONNECTED:
        case RoomDownload.ERROR:
        case RoomDownload.FILE_MISSING:
        case RoomDownload.INVALID_STATUS:
        case RoomDownload.NOT_DOWNLOADED:
        case RoomDownload.PAUSED:
        case RoomDownload.PENDING:
        case RoomDownload.PROGRESS:
        case RoomDownload.RETRY:
        case RoomDownload.STARTED:
        case RoomDownload.WARN:
          isIndeterminate = false;
          break;
        default:
          isIndeterminate = false;
      }
    }
    return isIndeterminate;
  }

  public Install.InstallationStatus mapDownloadState(RoomDownload download) {
    Install.InstallationStatus status = Install.InstallationStatus.UNINSTALLED;
    if (download != null) {
      switch (download.getOverallDownloadStatus()) {
        case RoomDownload.INVALID_STATUS:
          status = Install.InstallationStatus.INITIAL_STATE;
          break;
        case RoomDownload.FILE_MISSING:
        case RoomDownload.NOT_DOWNLOADED:
        case RoomDownload.COMPLETED:
          status = Install.InstallationStatus.UNINSTALLED;
          break;
        case RoomDownload.PAUSED:
          status = Install.InstallationStatus.PAUSED;
          break;
        case RoomDownload.ERROR:
          switch (download.getDownloadError()) {
            case RoomDownload.GENERIC_ERROR:
              status = Install.InstallationStatus.GENERIC_ERROR;
              break;
            case RoomDownload.NOT_ENOUGH_SPACE_ERROR:
              status = Install.InstallationStatus.NOT_ENOUGH_SPACE_ERROR;
              break;
          }
          break;
        case RoomDownload.RETRY:
        case RoomDownload.STARTED:
        case RoomDownload.WARN:
        case RoomDownload.CONNECTED:
        case RoomDownload.BLOCK_COMPLETE:
        case RoomDownload.PROGRESS:
        case RoomDownload.PENDING:
        case RoomDownload.WAITING_TO_MOVE_FILES:
        case RoomDownload.VERIFYING_FILE_INTEGRITY:
          status = Install.InstallationStatus.DOWNLOADING;
          break;
        case RoomDownload.IN_QUEUE:
          status = Install.InstallationStatus.IN_QUEUE;
          break;
      }
    } else {
      Logger.getInstance()
          .d(TAG, "mapping a null Download state");
    }
    return status;
  }

  private boolean mapInstallIndeterminate(int status, int type, RoomDownload download) {
    boolean isIndeterminate = false;
    switch (status) {
      case RoomInstalled.STATUS_UNINSTALLED:
      case RoomInstalled.STATUS_COMPLETED:
      case RoomInstalled.STATUS_WAITING_INSTALL_FEEDBACK:
        isIndeterminate = false;
        break;
      case RoomInstalled.STATUS_INSTALLING:
      case RoomInstalled.STATUS_ROOT_TIMEOUT:
        isIndeterminate = type != RoomInstalled.TYPE_DEFAULT;
        break;
      case RoomInstalled.STATUS_PRE_INSTALL:
        isIndeterminate =
            download != null && download.getOverallDownloadStatus() == RoomDownload.COMPLETED;
    }
    if (download != null && download.getOverallDownloadStatus() == RoomDownload.INVALID_STATUS) {
      isIndeterminate = true;
    }
    return isIndeterminate;
  }

  @NonNull
  private Completable updateDownloadAction(RoomDownload download, RoomDownload storedDownload) {
    if (storedDownload.getAction() != download.getAction()) {
      storedDownload.setAction(download.getAction());
      return downloadRepository.save(storedDownload);
    }
    return Completable.complete();
  }

  private Observable<Throwable> createDownloadAndRetry(Observable<? extends Throwable> errors,
      RoomDownload download) {
    return errors.flatMap(throwable -> {
      if (throwable instanceof DownloadNotFoundException) {
        Logger.getInstance()
            .d(TAG, "saved the newly created download because the other one was null");
        return downloadRepository.save(download)
            .andThen(Observable.just(throwable));
      } else {
        return Observable.error(throwable);
      }
    });
  }

  private Observable<String> startBackgroundInstallation(String md5, boolean forceDefaultInstall,
      boolean shouldSetPackageInstaller, boolean shouldInstall) {
    return aptoideDownloadManager.getDownloadAsSingle(md5)
        .doOnError(Throwable::printStackTrace)
        .toObservable()
        .doOnNext(__ -> {
          if (shouldInstall) {
          }
        })
        .flatMapCompletable(
            download -> initInstallationProgress(download).andThen(startDownload(download)))
        .map(__ -> md5);
  }

  private Completable startDownload(RoomDownload download) {
    if (download.getOverallDownloadStatus() == RoomDownload.COMPLETED) {
      Logger.getInstance()
          .d(TAG, "Saving an already completed download to trigger the download installation");
      return downloadRepository.save(download);
    } else {
      return aptoideDownloadManager.startDownload(download);
    }
  }

  private Completable initInstallationProgress(RoomDownload download) {
    RoomInstalled installed = convertDownloadToInstalled(download);
    return aptoideInstalledAppsRepository.save(installed);
  }

  @NonNull private RoomInstalled convertDownloadToInstalled(RoomDownload download) {
    RoomInstalled installed = new RoomInstalled();
    installed.setPackageAndVersionCode(download.getPackageName() + download.getVersionCode());
    installed.setVersionCode(download.getVersionCode());
    installed.setVersionName(download.getVersionName());
    installed.setAppSize(download.getSize());
    installed.setStatus(RoomInstalled.STATUS_PRE_INSTALL);
    installed.setType(RoomInstalled.TYPE_UNKNOWN);
    installed.setPackageName(download.getPackageName());
    return installed;
  }

  public Observable<Boolean> startInstalls(List<RoomDownload> downloads) {
    return Observable.from(downloads)
        .zipWith(Observable.interval(0, 1, TimeUnit.SECONDS), (download, along) -> download)
        .flatMapCompletable(download -> install(download))
        .toList()
        .map(installs -> true)
        .onErrorReturn(throwable -> false);
  }

  public Completable onAppInstalled(RoomInstalled installed) {
    return aptoideInstalledAppsRepository.getAsList(installed.getPackageName())
        .first()
        .flatMapIterable(installeds -> {
          //in case of installation made outside of aptoide
          if (installeds.isEmpty()) {
            installeds.add(installed);
          }
          return installeds;
        })
        .flatMapCompletable(databaseInstalled -> {
          if (databaseInstalled.getVersionCode() == installed.getVersionCode()) {
            installed.setType(databaseInstalled.getType());
            installed.setStatus(RoomInstalled.STATUS_COMPLETED);
            return aptoideInstalledAppsRepository.save(installed)
                .andThen(downloadRepository.remove(installed.getPackageName(),
                    installed.getVersionCode()));
          } else {
            return aptoideInstalledAppsRepository.remove(databaseInstalled.getPackageName(),
                databaseInstalled.getVersionCode());
          }
        })
        .toCompletable();
  }

  public Completable onAppRemoved(String packageName) {
    return aptoideInstalledAppsRepository.getAsList(packageName)
        .first()
        .flatMapIterable(installeds -> installeds)
        .flatMapCompletable(installed -> aptoideInstalledAppsRepository.remove(packageName,
            installed.getVersionCode()))
        .toCompletable();
  }

  private Observable<Install.InstallationType> getInstallationType(String packageName,
      int versionCode) {
    return aptoideInstalledAppsRepository.getInstalled(packageName)
        .map(installed -> {
          if (installed == null) {
            return Install.InstallationType.INSTALL;
          } else if (installed.getVersionCode() == versionCode) {
            return Install.InstallationType.INSTALLED;
          } else if (installed.getVersionCode() > versionCode) {
            return Install.InstallationType.DOWNGRADE;
          } else {
            return Install.InstallationType.UPDATE;
          }
        })
        .doOnNext(installationType -> Logger.getInstance()
            .d("AptoideDownloadManager", " emiting installation type"));
  }

  public Completable onUpdateConfirmed(RoomInstalled installed) {
    return onAppInstalled(installed);
  }

  /**
   * The caller is responsible to make sure that the download exists already this method should only
   * be used when a download exists already(ex: resuming)
   *
   * @return the download object to be resumed or null if doesn't exists
   */
  public Single<RoomDownload> getDownload(String md5) {
    return downloadRepository.getDownloadAsSingle(md5);
  }

  public Completable retryTimedOutInstallations() {
    return getTimedOutInstallations().first()
        .flatMapIterable(installs -> installs)
        .flatMapSingle(install -> getDownload(install.getMd5()))
        .flatMapCompletable(download -> defaultInstall(download))
        .toCompletable();
  }

  public Completable cleanTimedOutInstalls() {
    return getTimedOutInstallations().first()
        .flatMap(installs -> Observable.from(installs)
            .flatMap(install -> aptoideInstalledAppsRepository.get(install.getPackageName(),
                install.getVersionCode())
                .first()
                .flatMapCompletable(installed -> {
                  installed.setStatus(RoomInstalled.STATUS_UNINSTALLED);
                  return aptoideInstalledAppsRepository.save(installed);
                })))
        .toList()
        .toCompletable();
  }

  public Observable<List<RoomInstalled>> fetchInstalled() {
    return aptoideInstalledAppsRepository.getAllInstalledSorted()
        .first()
        .flatMapIterable(list -> list)
        .filter(item -> !item.isSystemApp())
        .toList();
  }

  public Observable<List<RoomInstalled>> fetchInstalledExceptSystem() {
    return aptoideInstalledAppsRepository.getInstalledAppsFilterSystem();
  }

  public Observable<Boolean> isInstalled(String packageName) {
    return aptoideInstalledAppsRepository.isInstalled(packageName)
        .first();
  }

  public Single<Install> filterInstalled(Install item) {
    return aptoideInstalledAppsRepository.isInstalled(item.getPackageName(), item.getVersionCode())
        .flatMap(isInstalled -> {
          if (isInstalled) {
            return Single.just(null);
          }
          return Single.just(item);
        });
  }

  public boolean wasAppEverInstalled(String packageName) {
    return aptoideInstalledAppsRepository.getInstallationsHistory()
        .first()
        .flatMapIterable(installation -> installation)
        .filter(installation -> packageName.equals(installation.getPackageName()))
        .toList()
        .flatMap(installations -> {
          if (installations.isEmpty()) {
            return Observable.just(Boolean.FALSE);
          } else {
            return Observable.just(Boolean.TRUE);
          }
        })
        .toBlocking()
        .first();
  }

  public Observable<Install.InstallationStatus> getDownloadState(String md5) {
    return aptoideDownloadManager.getDownloadAsObservable(md5)
        .first()
        .map(download -> mapDownloadState(download));
  }

  public Single<Boolean> hasNextDownload() {
    return aptoideDownloadManager.getCurrentActiveDownloads()
        .first()
        .map(downloads -> downloads != null && !downloads.isEmpty())
        .toSingle();
  }

  public Single<Long> getInstalledAppSize(@Nullable String packageName) {
    return aptoideInstalledAppsRepository.getInstalled(packageName)
        .first()
        .toSingle()
        .map(app -> app.getAppSize());
  }

  public Observable<List<String>> getDownloadOutOfSpaceMd5List() {
    return downloadRepository.getOutOfSpaceDownloads()
        .filter(list -> !list.isEmpty())
        .flatMap(list -> Observable.from(list)
            .map(download -> download.getMd5())
            .toList());
  }
}
