package com.asfoundation.wallet.repository;

import com.appcoins.wallet.commons.MemoryCache;
import com.appcoins.wallet.ui.arch.RxSchedulers;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.util.FakeSchedulers;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class BdsPendingTransactionServiceTest {

  public static final String ORDER_REFERENCE = "order_reference";
  private static final String PACKAGE_NAME = "package_name";
  private static final String SKU = "sku";
  private static final String UID = "uid";
  private static final String PURCHASE_UID = "purchase_uid";
  private static final String KEY = "key";
  @Mock BdsPendingTransactionService transactionService;
  private BdsTransactionService bdsPendingTransactionService;
  private final RxSchedulers fakeSchedulers = new FakeSchedulers();

  @Before public void setUp() {
    when(transactionService.checkTransactionStateFromTransactionId(UID)).thenReturn(
        Observable.just(new PendingTransaction(KEY, true), new PendingTransaction(KEY, false)));
    bdsPendingTransactionService = new BdsTransactionService(fakeSchedulers,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()),
        new CompositeDisposable(), transactionService);
  }

  @Test public void getTransaction() {
    TestScheduler scheduler = ((TestScheduler) fakeSchedulers.getMain());
    bdsPendingTransactionService.start();
    scheduler.triggerActions();

    TestObserver<BdsTransactionService.BdsTransaction> transactionObserver = new TestObserver<>();
    bdsPendingTransactionService.getTransaction(KEY)
        .subscribe(transactionObserver);
    scheduler.advanceTimeBy(3, TimeUnit.SECONDS);
    scheduler.triggerActions();

    TestObserver<Object> observer = new TestObserver<>();
    bdsPendingTransactionService.trackTransaction(KEY, PACKAGE_NAME, SKU, UID, PURCHASE_UID,
        ORDER_REFERENCE)
        .subscribeOn(fakeSchedulers.getMain())
        .subscribe(observer);
    scheduler.advanceTimeBy(3, TimeUnit.SECONDS);
    scheduler.triggerActions();
    scheduler.advanceTimeBy(3, TimeUnit.SECONDS);
    scheduler.triggerActions();

    observer.assertNoErrors()
        .assertComplete();

    BdsTransactionService.BdsTransaction bdsTransaction =
        new BdsTransactionService.BdsTransaction(UID, PURCHASE_UID, KEY, PACKAGE_NAME, SKU,
            ORDER_REFERENCE);
    transactionObserver.assertNoErrors()
        .assertValues(new BdsTransactionService.BdsTransaction(bdsTransaction,
                BdsTransactionService.BdsTransaction.Status.PROCESSING),
            new BdsTransactionService.BdsTransaction(bdsTransaction,
                BdsTransactionService.BdsTransaction.Status.PROCESSING),
            new BdsTransactionService.BdsTransaction(bdsTransaction,
                BdsTransactionService.BdsTransaction.Status.COMPLETED));
  }
}