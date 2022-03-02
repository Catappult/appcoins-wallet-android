package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationEntity;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class AppCoinsOperationMapper {

  public @Inject AppCoinsOperationMapper(){
  }

  public List<AppCoinsOperation> map(List<AppCoinsOperationEntity> appCoinsOperationEntities) {
    List<AppCoinsOperation> list = new ArrayList<>();
    for (AppCoinsOperationEntity appCoinsOperationEntity : appCoinsOperationEntities) {
      list.add(map(appCoinsOperationEntity));
    }
    return list;
  }

  public AppCoinsOperation map(AppCoinsOperationEntity appCoinsOperationEntity) {
    if (appCoinsOperationEntity != null) {
      return new AppCoinsOperation(appCoinsOperationEntity.getTransactionId(),
          appCoinsOperationEntity.getPackageName(), appCoinsOperationEntity.getApplicationName(),
          appCoinsOperationEntity.getIconPath(), appCoinsOperationEntity.getProductName());
    }
    return null;
  }

  public AppCoinsOperationEntity map(String key, AppCoinsOperation appCoinsOperation) {
    return new AppCoinsOperationEntity(key, appCoinsOperation.getTransactionId(),
        appCoinsOperation.getPackageName(), appCoinsOperation.getApplicationName(),
        appCoinsOperation.getIconPath(), appCoinsOperation.getProductName());
  }
}
