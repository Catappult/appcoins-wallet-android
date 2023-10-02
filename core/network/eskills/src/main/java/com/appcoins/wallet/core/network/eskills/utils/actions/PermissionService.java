package com.appcoins.wallet.core.network.eskills.utils.actions;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import rx.functions.Action0;

public interface PermissionService {

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToAccounts(
      @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied);

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToAccounts(boolean forceShowRationale,
      @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied);

  @TargetApi(Build.VERSION_CODES.M) void requestDownloadAccess(
      @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied,
      boolean shouldValidateMobileData, boolean canBypassWifi, long size);

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToCamera(
      @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied);

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToExternalFileSystem(
      @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied);

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToExternalFileSystem(
      boolean forceShowRationale, @Nullable Action0 toRunWhenAccessIsGranted,
      @Nullable Action0 toRunWhenAccessIsDenied);

  @TargetApi(Build.VERSION_CODES.M) void requestAccessToExternalFileSystem(
      boolean forceShowRationale, @StringRes int rationaleMessage,
      @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDennied);

  @TargetApi(Build.VERSION_CODES.M) void hasDownloadAccess(@Nullable Action0 accessGranted,
      @Nullable Action0 accessDenied);
}
