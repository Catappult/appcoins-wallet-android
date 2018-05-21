package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationEntity;
import java.util.ArrayList;
import java.util.List;

public class AppCoinsOperationMapper {
  public List<AppCoinsOperation> map(List<AppCoinsOperationEntity> appCoinsOperationEntities) {
    List<AppCoinsOperation> list = new ArrayList<>();
    for (AppCoinsOperationEntity appCoinsOperationEntity : appCoinsOperationEntities) {
      list.add(map(appCoinsOperationEntity));
    }
    return list;
  }

  public AppCoinsOperation map(AppCoinsOperationEntity appCoinsOperationEntity) {
    return new AppCoinsOperation(appCoinsOperationEntity.getTransactionId(),
        appCoinsOperationEntity.getPackageName(), appCoinsOperationEntity.getApplicationName(),
        appCoinsOperationEntity.getIconPath(), appCoinsOperationEntity.getProductName());
  }

  public AppCoinsOperationEntity map(String key, AppCoinsOperation appCoinsOperation) {
    return new AppCoinsOperationEntity(key, appCoinsOperation.getTransactionId(),
        appCoinsOperation.getPackageName(), appCoinsOperation.getApplicationName(),
        appCoinsOperation.getIconPath(), appCoinsOperation.getProductName());
  }
}
