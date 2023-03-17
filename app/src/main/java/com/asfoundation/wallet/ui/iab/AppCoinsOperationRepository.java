package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.core.utils.jvm_common.Repository;
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDao;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import it.czerwinski.android.hilt.annotations.BoundTo;
import java.util.List;
import javax.inject.Inject;

@BoundTo(supertype = Repository.class)
public class AppCoinsOperationRepository implements Repository<String, AppCoinsOperation> {
  private final AppCoinsOperationDao inAppPurchaseDataDao;
  private final AppCoinsOperationMapper mapper;

  public @Inject AppCoinsOperationRepository(AppCoinsOperationDao inAppPurchaseDataDao,
      AppCoinsOperationMapper mapper) {
    this.inAppPurchaseDataDao = inAppPurchaseDataDao;
    this.mapper = mapper;
  }

  @Override public Completable save(String key, AppCoinsOperation value) {
    return Completable.fromAction(() -> saveSync(key, value));
  }

  @Override public Observable<List<AppCoinsOperation>> getAll() {
    return inAppPurchaseDataDao.getAllAsFlowable()
        .toObservable()
        .map(mapper::map);
  }

  @Override public Observable<AppCoinsOperation> get(String key) {
    return inAppPurchaseDataDao.getAsFlowable(key)
        .toObservable()
        .map(mapper::map);
  }

  @Override public Completable remove(String key) {
    return Completable.fromAction(() -> removeSync(key));
  }

  @Override public Single<Boolean> contains(String key) {
    return Single.just(containsSync(key));
  }

  @Override public void saveSync(String key, AppCoinsOperation value) {
    inAppPurchaseDataDao.insert(mapper.map(key, value));
  }

  @Override public List<AppCoinsOperation> getAllSync() {
    return mapper.map(inAppPurchaseDataDao.getAll());
  }

  @Override public AppCoinsOperation getSync(String key) {
    return mapper.map(inAppPurchaseDataDao.get(key));
  }

  @Override public void removeSync(String key) {
    inAppPurchaseDataDao.delete(inAppPurchaseDataDao.get(key));
  }

  @Override public boolean containsSync(String key) {
    return inAppPurchaseDataDao.get(key) != null;
  }
}
