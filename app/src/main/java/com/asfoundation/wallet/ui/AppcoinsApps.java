package com.asfoundation.wallet.ui;

import com.asfoundation.wallet.apps.App;
import com.asfoundation.wallet.apps.Applications;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;

public class AppcoinsApps {
  private final Applications applications;

  public AppcoinsApps(Applications applications) {
    this.applications = applications;
  }

  public Single<List<AppcoinsApplication>> getApps() {
    return applications.getApps()
        .map(this::map);
  }

  private List<AppcoinsApplication> map(List<App> apps) {
    ArrayList<AppcoinsApplication> appcoinsApplications = new ArrayList<>();
    for (App app : apps) {
      appcoinsApplications.add(
          new AppcoinsApplication(app.getName(), app.getRating(), app.getIconUrl(),
              app.getFeaturedGraphic(), app.getPackageName()));
    }
    return appcoinsApplications;
  }
}
