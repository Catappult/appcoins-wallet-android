package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.repository.Repository;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperation;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDao;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public class AppCoinsOperationRepository implements Repository<String, AppCoinsOperation> {
  private final AppCoinsOperationDao inAppPurchaseDataDao;

  public AppCoinsOperationRepository(AppCoinsOperationDao inAppPurchaseDataDao) {
    this.inAppPurchaseDataDao = inAppPurchaseDataDao;
  }

  @Override public Completable save(String key, AppCoinsOperation value) {
    return Completable.fromAction(() -> saveSync(key, value));
  }

  @Override public Observable<List<AppCoinsOperation>> getAll() {
    return inAppPurchaseDataDao.getAllAsFlowable()
        .toObservable();
  }

  @Override public Observable<AppCoinsOperation> get(String key) {
    return inAppPurchaseDataDao.getAsFlowable(key)
        .toObservable();
  }

  @Override public Completable remove(String key) {
    return Completable.fromAction(() -> removeSync(key));
  }

  @Override public Single<Boolean> contains(String key) {
    return Single.just(containsSync(key));
  }

  @Override public void saveSync(String key, AppCoinsOperation value) {
    inAppPurchaseDataDao.insert(value);
  }

  @Override public List<AppCoinsOperation> getAllSync() {
    return inAppPurchaseDataDao.getAll();
  }

  @Override public AppCoinsOperation getSync(String key) {
    return inAppPurchaseDataDao.get(key);
  }

  @Override public void removeSync(String key) {
    inAppPurchaseDataDao.delete(inAppPurchaseDataDao.get(key));
  }

  @Override public boolean containsSync(String key) {
    return inAppPurchaseDataDao.get(key) != null;
  }
}
