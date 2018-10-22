package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.commons.MemoryCache;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class AppCoinsOperationDataSaverTest {
  public static final String PACKAGE_NAME = "PACKAGE_NAME";
  public static final String APPLICATION_NAME = "application_name";
  public static final String PATH = "path";
  public static final String PRODUCT_NAME = "product_name";
  public static final String ID_1 = "id1";
  public static final String ID_2 = "id_2";
  @Mock AppInfoProvider appInfoProvider;
  private BehaviorSubject<AppcoinsOperationsDataSaver.OperationDataSource.OperationData>
      operationDataSource1;
  private BehaviorSubject<AppcoinsOperationsDataSaver.OperationDataSource.OperationData>
      operationDataSource2;
  private AppcoinsOperationsDataSaver dataSaver;
  private TestScheduler scheduler;
  private MemoryCache<String, AppCoinsOperation> cache;

  @Before public void before()
      throws AppInfoProvider.UnknownApplicationException, ImageSaver.SaveException {
    operationDataSource1 = BehaviorSubject.create();
    operationDataSource2 = BehaviorSubject.create();

    when(appInfoProvider.get(anyString(), anyString(), anyString())).thenAnswer(
        invocation -> new AppCoinsOperation(invocation.getArgument(0), invocation.getArgument(1),
            APPLICATION_NAME, PATH, invocation.getArgument(2)));

    scheduler = new TestScheduler();
    cache = new MemoryCache<>(BehaviorSubject.create(), new HashMap<>());

    List<AppcoinsOperationsDataSaver.OperationDataSource> list = new ArrayList<>();
    list.add(() -> operationDataSource1);
    list.add(() -> operationDataSource2);
    dataSaver = new AppcoinsOperationsDataSaver(list, cache, appInfoProvider, scheduler,
        new CompositeDisposable());
  }

  @Test public void start() {
    dataSaver.start();
    operationDataSource1.onNext(
        new AppcoinsOperationsDataSaver.OperationDataSource.OperationData(ID_1, PACKAGE_NAME,
            PRODUCT_NAME));
    operationDataSource2.onNext(
        new AppcoinsOperationsDataSaver.OperationDataSource.OperationData(ID_2, PACKAGE_NAME,
            PRODUCT_NAME));
    scheduler.triggerActions();
    scheduler.triggerActions();
    Assert.assertEquals(
        new AppCoinsOperation(ID_1, PACKAGE_NAME, APPLICATION_NAME, PATH, PRODUCT_NAME),
        cache.getSync(ID_1));
    Assert.assertEquals(
        new AppCoinsOperation(ID_2, PACKAGE_NAME, APPLICATION_NAME, PATH, PRODUCT_NAME),
        cache.getSync(ID_2));
  }

  @Test public void addAfterStop() {
    dataSaver.start();
    dataSaver.stop();

    operationDataSource1.onNext(
        new AppcoinsOperationsDataSaver.OperationDataSource.OperationData(ID_1, PACKAGE_NAME,
            PRODUCT_NAME));
    scheduler.triggerActions();
    Assert.assertEquals(null, cache.getSync(ID_1));
  }
}