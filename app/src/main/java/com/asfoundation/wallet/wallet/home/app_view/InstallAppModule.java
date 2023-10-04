package com.asfoundation.wallet.wallet.home.app_view;

import android.content.Context;
import android.os.Environment;
import com.appcoins.wallet.core.network.eskills.download.FileDownloadManagerProvider;
import com.appcoins.wallet.core.network.eskills.download.Md5Comparator;
import com.appcoins.wallet.core.network.eskills.install.AppInstallerStatusReceiver;
import com.appcoins.wallet.core.network.eskills.packageinstaller.AppInstaller;
import com.appcoins.wallet.core.network.eskills.utils.utils.FileUtils;
import com.asfoundation.wallet.App;
import com.liulishuo.filedownloader.FileDownloader;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.subjects.PublishSubject;

@InstallIn(SingletonComponent.class) @Module public class InstallAppModule {

  @Inject App application;

  @Singleton @Provides AppInstaller providesAppInstaller(@ApplicationContext Context context,
      AppInstallerStatusReceiver appInstallerStatusReceiver) {
    return new AppInstaller(context,
        (installStatus) -> appInstallerStatusReceiver.onStatusReceived(installStatus));
  }

  @Singleton @Provides AppInstallerStatusReceiver providesAppInstallerStatusReceiver() {
    return new AppInstallerStatusReceiver(PublishSubject.create());
  }

  @Singleton @Provides @Named("apkPath") String provideApkPath(
      @Named("cachePath") String cachePath) {
    return cachePath + "apks/";
  }

  @Singleton @Provides @Named("cachePath") String provideCachePath() {
    return Environment.getExternalStorageDirectory()
        .getAbsolutePath() + "/.aptoide/";
  }

  @Singleton @Provides @Named("obbPath") String provideObbPath(
      @Named("cachePath") String cachePath) {
    return cachePath + "obb/";
  }

  @Provides @Singleton FileDownloadManagerProvider providesFileDownloaderProvider(
      @Named("cachePath") String cachePath, @ApplicationContext Context context,
      Md5Comparator md5Comparator) {

    FileUtils.createDir(cachePath);
    FileDownloader.init(context.getApplicationContext());
    return new FileDownloadManagerProvider(cachePath, FileDownloader.getImpl(), md5Comparator);
  }

  @Singleton @Provides Md5Comparator providesMd5Comparator(@Named("cachePath") String cachePath) {
    return new Md5Comparator(cachePath);
  }

  @Singleton @Provides FileUtils provideFileUtils() {
    return new FileUtils();
  }
}


