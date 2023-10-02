package com.appcoins.wallet.core.network.eskills.utils.utils.q;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Base64;
import android.view.WindowManager;
import com.appcoins.wallet.core.network.eskills.utils.utils.AptoideUtils.ScreenU;
import com.appcoins.wallet.core.network.eskills.utils.utils.AptoideUtils.SystemU;

/**
 * Created by neuro on 12-05-2017.
 */
public class QManager {

  private final Resources resources;
  private final ActivityManager activityManager;
  private final WindowManager windowManager;
  private Integer minSdk;
  private String cpuAbi;
  private String screenSize;
  private String glEs;
  private Integer densityDpi;
  private String cachedFilters;
  private UiModeManager uiModeManager;

  public QManager(Resources resources, ActivityManager activityManager, WindowManager windowManager,
      UiModeManager uiModeManager) {
    this.resources = resources;
    this.activityManager = activityManager;
    this.windowManager = windowManager;
    this.uiModeManager = uiModeManager;
  }

  private Integer getMinSdk() {
    if (minSdk == null) {
      minSdk = computeMinSdk();
    }
    return minSdk;
  }

  private String getCpuAbi() {
    if (cpuAbi == null) {
      cpuAbi = computeCpuAbi();
    }
    return cpuAbi;
  }

  private String getScreenSize() {
    if (screenSize == null) {
      screenSize = computeScreenSize();
    }
    return screenSize;
  }

  private String getGlEs() {
    if (glEs == null) {
      glEs = computeGlEs();
    }
    return glEs;
  }

  private Integer getDensityDpi() {
    if (densityDpi == null) {
      densityDpi = computeDensityDpi();
    }
    return densityDpi;
  }

  private int computeMinSdk() {
    return SystemU.getSdkVer();
  }

  private String computeScreenSize() {
    return ScreenU.getScreenSize(resources);
  }

  private String computeGlEs() {
    return SystemU.getGlEsVer(activityManager);
  }

  private int computeDensityDpi() {
    return ScreenU.getDensityDpi(windowManager);
  }

  private String computeCpuAbi() {
    return SystemU.getAbis();
  }

  public String getFilters(boolean hwSpecsFilter) {
    if (!hwSpecsFilter) {
      return null;
    }

    if (cachedFilters == null) {
      cachedFilters = computeFilters();
    }

    return cachedFilters;
  }

  private String hasLeanback() {
    if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
      return "1";
    } else {
      return "0";
    }
  }

  private String computeFilters() {
    String filters = "maxSdk="
        + getMinSdk()
        + "&maxScreen="
        + getScreenSize()
        + "&maxGles="
        + getGlEs()
        + "&myCPU="
        + getCpuAbi()
        + "&leanback="
        + hasLeanback()
        + "&myDensity="
        + getDensityDpi();

    return Base64.encodeToString(filters.getBytes(), 0)
        .replace("=", "")
        .replace("/", "*")
        .replace("+", "_")
        .replace("\n", "");
  }
}
