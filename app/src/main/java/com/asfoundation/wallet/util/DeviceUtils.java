package com.asfoundation.wallet.util;

import android.os.Build;

public class DeviceUtils {

  public static String getDeviceManufacturer() {
    return Build.MANUFACTURER;
  }

  public static String getDeviceModel() {
    return Build.MODEL;
  }
}
