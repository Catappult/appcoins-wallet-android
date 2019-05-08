package com.asfoundation.wallet.ui;

import com.asfoundation.wallet.apps.Application;
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

  private List<AppcoinsApplication> map(List<Application> apps) {
    ArrayList<AppcoinsApplication> appcoinsApplications = new ArrayList<>();
    for (Application app : apps) {
      appcoinsApplications.add(
          new AppcoinsApplication(app.getName(), app.getRating(), app.getIconUrl(),
              app.getFeaturedGraphic(), app.getPackageName(), app.getUniqueName()));
    }
    return appcoinsApplications;
  }
}
