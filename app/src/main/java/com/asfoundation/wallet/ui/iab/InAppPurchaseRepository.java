package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.repository.Cache;
import com.asfoundation.wallet.ui.iab.database.InAppPurchaseData;
import com.asfoundation.wallet.ui.iab.database.InAppPurchaseDataDao;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public class InAppPurchaseRepository implements Cache<String, InAppPurchaseData> {
  private final InAppPurchaseDataDao inAppPurchaseDataDao;

  public InAppPurchaseRepository(InAppPurchaseDataDao inAppPurchaseDataDao) {
    this.inAppPurchaseDataDao = inAppPurchaseDataDao;
  }

  @Override public Completable save(String key, InAppPurchaseData value) {
    return Completable.fromAction(() -> saveSync(key, value));
  }

  @Override public Observable<List<InAppPurchaseData>> getAll() {
    return inAppPurchaseDataDao.getAllAsFlowable()
        .toObservable();
  }

  @Override public Observable<InAppPurchaseData> get(String key) {
    return inAppPurchaseDataDao.getAsFlowable(key)
        .toObservable();
  }

  @Override public Completable remove(String key) {
    return Completable.fromAction(() -> removeSync(key));
  }

  @Override public Single<Boolean> contains(String key) {
    return Single.just(containsSync(key));
  }

  @Override public void saveSync(String key, InAppPurchaseData value) {
    inAppPurchaseDataDao.insert(value);
  }

  @Override public List<InAppPurchaseData> getAllSync() {
    return inAppPurchaseDataDao.getAll();
  }

  @Override public InAppPurchaseData getSync(String key) {
    return inAppPurchaseDataDao.get(key);
  }

  @Override public void removeSync(String key) {
    inAppPurchaseDataDao.delete(inAppPurchaseDataDao.get(key));
  }

  @Override public boolean containsSync(String key) {
    return inAppPurchaseDataDao.get(key) != null;
  }
}
