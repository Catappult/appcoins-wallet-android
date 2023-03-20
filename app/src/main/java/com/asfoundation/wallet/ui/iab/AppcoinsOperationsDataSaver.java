package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.core.utils.jvm_common.Repository;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;

public class AppcoinsOperationsDataSaver {
  private final Repository<String, AppCoinsOperation> cache;
  private final AppInfoProvider appInfoProvider;
  private final Scheduler scheduler;
  private final CompositeDisposable disposables;
  private List<OperationDataSource> operationDataSourceList;

  public AppcoinsOperationsDataSaver(List<OperationDataSource> operationDataSourceList,
      Repository<String, AppCoinsOperation> cache, AppInfoProvider appInfoProvider,
      Scheduler scheduler, CompositeDisposable disposables) {
    this.operationDataSourceList = operationDataSourceList;
    this.cache = cache;
    this.appInfoProvider = appInfoProvider;
    this.scheduler = scheduler;
    this.disposables = disposables;
  }

  public void start() {
    disposables.add(Single.fromCallable(() -> {
      List<Observable<OperationDataSource.OperationData>> list = new ArrayList<>();
      for (OperationDataSource operationDataSource : operationDataSourceList) {
        list.add(operationDataSource.get());
      }
      return list;
    })
        .observeOn(scheduler)
        .toObservable()
        .flatMap(Observable::merge)
        .flatMap(
            operationData -> getAppData(operationData.getHash(), operationData.getPackageName(),
                operationData.getData()))
        .doOnNext(inAppPurchaseData -> cache.saveSync(inAppPurchaseData.getTransactionId(),
            inAppPurchaseData))
        .doOnError(Throwable::printStackTrace)
        .retryWhen(this::isKnownErrorError)
        .subscribe());
  }

  private ObservableSource<AppCoinsOperation> getAppData(String hash, String packageName,
      String data) {
    try {
      return Observable.just(appInfoProvider.get(hash, packageName, data));
    } catch (ImageSaver.SaveException | AppInfoProvider.UnknownApplicationException e) {
      return Observable.error(e);
    }
  }

  private Observable<Object> isKnownErrorError(Observable<Throwable> throwableObservable) {
    return throwableObservable.flatMap(throwable -> {
      if (throwable instanceof ImageSaver.SaveException
          || throwable instanceof AppInfoProvider.UnknownApplicationException) {
        return Observable.just(true);
      }
      return Observable.error(throwable);
    });
  }

  public Observable<AppCoinsOperation> get(String id) {
    return cache.get(id);
  }

  public AppCoinsOperation getSync(String id) {
    return cache.getSync(id);
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
  }

  public Observable<List<AppCoinsOperation>> getAll() {
    return cache.getAll();
  }

  public interface OperationDataSource {
    Observable<OperationData> get();

    class OperationData {
      private final String hash;
      private final String packageName;
      private final String data;

      public OperationData(String hash, String packageName, String data) {
        this.hash = hash;
        this.packageName = packageName;
        this.data = data;
      }

      public String getHash() {
        return hash;
      }

      public String getPackageName() {
        return packageName;
      }

      public String getData() {
        return data;
      }
    }
  }
}

