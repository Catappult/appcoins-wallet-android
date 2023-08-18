package com.asfoundation.wallet.wallet.home.app_view;

import android.content.SharedPreferences;
import android.os.Environment;
import cm.aptoide.pt.app.aptoideinstall.AptoideInstallManager;
import cm.aptoide.pt.download.AppValidator;
import cm.aptoide.pt.download.SplitTypeSubFileTypeMapper;
import cm.aptoide.pt.install.FilePathProvider;
import cm.aptoide.pt.install.InstallAppSizeValidator;
import com.appcoins.wallet.core.network.eskills.download.DownloadApkPathsProvider;
import com.appcoins.wallet.core.network.eskills.download.DownloadFactory;
import com.appcoins.wallet.core.network.eskills.download.FileDownloadManagerProvider;
import com.appcoins.wallet.core.network.eskills.download.Md5Comparator;
import com.appcoins.wallet.core.network.eskills.downloadmanager.AppDownloaderProvider;
import com.appcoins.wallet.core.network.eskills.downloadmanager.AptoideDownloadManager;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadAnalytics;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadAppFileMapper;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadAppMapper;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadPersistence;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadStatusMapper;
import com.appcoins.wallet.core.network.eskills.downloadmanager.DownloadsRepository;
import com.appcoins.wallet.core.network.eskills.downloadmanager.FileDownloaderProvider;
import com.appcoins.wallet.core.network.eskills.downloadmanager.RetryFileDownloadManagerProvider;
import com.appcoins.wallet.core.network.eskills.downloadmanager.RetryFileDownloaderProvider;
import com.appcoins.wallet.core.network.eskills.install.AppInstallerStatusReceiver;
import com.appcoins.wallet.core.network.eskills.install.AptoideInstalledAppsRepository;
import com.appcoins.wallet.core.network.eskills.install.InstallManager;
import com.appcoins.wallet.core.network.eskills.install.PackageInstallerManager;
import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstaller;
import com.appcoins.wallet.core.network.eskills.utils.utils.AptoideUtils;
import com.appcoins.wallet.core.network.eskills.utils.utils.FileUtils;
import com.asfoundation.wallet.App;
import com.liulishuo.filedownloader.FileDownloader;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.subjects.PublishSubject;

@InstallIn(SingletonComponent.class)
@Module public class InstallAppModule {

  private final App application;

  public InstallAppModule(){this.application=null;}

  public InstallAppModule(App application) {
    this.application = application;
  }

  @Singleton @Provides InstallManager providesInstallManager(
      AptoideDownloadManager aptoideDownloadManager, AppInstaller appInstaller,
      @Named("default") SharedPreferences defaultSharedPreferences,
      @Named("secureShared") SharedPreferences secureSharedPreferences,
      DownloadsRepository downloadsRepository,
      AptoideInstalledAppsRepository aptoideInstalledAppsRepository,
      PackageInstallerManager packageInstallerManager, AptoideInstallManager aptoideInstallManager,
      InstallAppSizeValidator installAppSizeValidator) {
    return new InstallManager(aptoideDownloadManager, appInstaller, defaultSharedPreferences,
        secureSharedPreferences, downloadsRepository, aptoideInstalledAppsRepository,
        packageInstallerManager, aptoideInstallManager, installAppSizeValidator);
  }

  @Singleton @Provides AppInstaller providesAppInstaller(
      AppInstallerStatusReceiver appInstallerStatusReceiver) {
    return new AppInstaller(application.getApplicationContext(),
        (installStatus) -> appInstallerStatusReceiver.onStatusReceived(installStatus));
  }

  @Singleton @Provides AppInstallerStatusReceiver providesAppInstallerStatusReceiver() {
    return new AppInstallerStatusReceiver(PublishSubject.create());
  }

  @Singleton @Provides PackageInstallerManager providesPackageInstallerManager() {
    return new PackageInstallerManager(AptoideUtils.isDeviceMIUI(),
        AptoideUtils.isMIUIwithAABFix());
  }

  @Singleton @Provides InstallAppSizeValidator providesInstallAppSizeValidator(
      FilePathProvider filePathProvider) {
    return new InstallAppSizeValidator(filePathProvider);
  }

  @Singleton @Provides FilePathProvider filePathManager(@Named("cachePath") String cachePath,
      @Named("apkPath") String apkPath, @Named("obbPath") String obbPath) {
    return new FilePathProvider(apkPath, obbPath, cachePath);
  }

  @Singleton @Provides @Named("apkPath") String provideApkPath(
      @Named("cachePath") String cachePath) {
    return cachePath + "apks/";
  }

  @Singleton @Provides @Named("cachePath") String provideCachePath() {
    return Environment.getExternalStorageDirectory()
        .getAbsolutePath() + "/.wallet/";
  }

  @Singleton @Provides @Named("obbPath") String provideObbPath(
      @Named("cachePath") String cachePath) {
    return cachePath + "obb/";
  }

  @Singleton @Provides AptoideDownloadManager provideAptoideDownloadManager(
      DownloadsRepository downloadsRepository, DownloadStatusMapper downloadStatusMapper,
      @Named("cachePath") String cachePath, DownloadAppMapper downloadAppMapper,
      AppDownloaderProvider appDownloaderProvider, @Named("apkPath") String apkPath,
      @Named("obbPath") String obbPath, DownloadAnalytics downloadAnalytics,
      FilePathProvider filePathProvider) {
    FileUtils.createDir(apkPath);
    FileUtils.createDir(obbPath);
    return new AptoideDownloadManager(downloadsRepository, downloadStatusMapper, cachePath,
        downloadAppMapper, appDownloaderProvider, downloadAnalytics, new FileUtils(),
        filePathProvider);
  }

  @Provides @Singleton DownloadAppFileMapper providesDownloadAppFileMapper() {
    return new DownloadAppFileMapper();
  }

  @Singleton @Provides DownloadAppMapper providesDownloadAppMapper(
      DownloadAppFileMapper downloadAppFileMapper) {
    return new DownloadAppMapper(downloadAppFileMapper);
  }

  @Provides @Singleton FileDownloaderProvider providesFileDownloaderProvider(
      @Named("cachePath") String cachePath,
      Md5Comparator md5Comparator) {


    return new FileDownloadManagerProvider(cachePath, FileDownloader.getImpl(), md5Comparator);
  }

  @Singleton @Provides Md5Comparator providesMd5Comparator(@Named("cachePath") String cachePath) {
    return new Md5Comparator(cachePath);
  }

  @Singleton @Provides AppDownloaderProvider providesAppDownloaderProvider(
      RetryFileDownloaderProvider fileDownloaderProvider, DownloadAnalytics downloadAnalytics) {
    return new AppDownloaderProvider(fileDownloaderProvider, downloadAnalytics);
  }

  @Singleton @Provides RetryFileDownloaderProvider providesRetryFileDownloaderProvider(
      FileDownloaderProvider fileDownloaderProvider) {
    return new RetryFileDownloadManagerProvider(fileDownloaderProvider);
  }

  @Singleton @Provides DownloadsRepository provideDownloadsRepository(
      DownloadPersistence downloadPersistence) {
    return new DownloadsRepository(downloadPersistence);
  }

  @Singleton @Provides DownloadStatusMapper downloadStatusMapper() {
    return new DownloadStatusMapper();
  }

  @Singleton @Provides FileUtils provideFileUtils() {
    return new FileUtils();
  }

  @Singleton @Provides DownloadFactory provideDownloadFactory(
      @Named("marketName") String marketName, DownloadApkPathsProvider downloadApkPathsProvider,
      @Named("cachePath") String cachePath, AppValidator appValidator,
      SplitTypeSubFileTypeMapper splitTypeSubFileTypeMapper) {
    return new DownloadFactory(marketName, downloadApkPathsProvider, cachePath, appValidator,
        splitTypeSubFileTypeMapper);
  }

  @Singleton @Provides SplitTypeSubFileTypeMapper provideSplitTypeSubFileTypeMapper() {
    return new SplitTypeSubFileTypeMapper();
  }
}


