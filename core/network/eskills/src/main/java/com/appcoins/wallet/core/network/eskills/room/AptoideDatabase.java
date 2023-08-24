package com.appcoins.wallet.core.network.eskills.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Database holder that contains the list of entities (tables) associated with the database.
 */
@Database(entities = {
    RoomEvent.class, RoomExperiment.class, RoomStoredMinimalAd.class, RoomNotification.class,
    RoomLocalNotificationSync.class, RoomInstalled.class, RoomInstallation.class,
    RoomMigratedApp.class, RoomUpdate.class, RoomDownload.class, RoomStore.class,
    RoomAptoideInstallApp.class, RoomAppComingSoonRegistration.class
}, version = AptoideDatabase.VERSION) @TypeConverters({
    SplitTypeConverter.class, StringTypeConverter.class, FileToDownloadTypeConverter.class
}) public abstract class AptoideDatabase extends RoomDatabase {

  /**
   * Database Schema version
   */
  static final int VERSION = 107;

  public abstract EventDAO eventDAO();

  public abstract ExperimentDAO experimentDAO();

  public abstract StoredMinimalAdDAO storeMinimalAdDAO();

  public abstract NotificationDao notificationDao();

  public abstract LocalNotificationSyncDao localNotificationSyncDao();

  public abstract InstalledDao installedDao();

  public abstract InstallationDao installationDao();

  public abstract MigratedAppDAO migratedAppDAO();

  public abstract UpdateDao updateDao();

  public abstract DownloadDAO downloadDAO();

  public abstract StoreDao storeDao();

  public abstract AptoideInstallDao aptoideInstallDao();

  public abstract AppComingSoonRegistrationDAO appComingSoonRegistrationDAO();
}
