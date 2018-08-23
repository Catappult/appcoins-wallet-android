package com.asfoundation.wallet.repository;

import com.appcoins.wallet.billing.repository.entity.Gateway;
import com.appcoins.wallet.billing.repository.entity.Transaction;
import io.reactivex.Single;
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

  public static final String PACKAGE_NAME = "package_name";
  public static final String SKU = "sku";
  public static final String UID = "uid";
  public static final String KEY = "key";
  @Mock BdsTransactionProvider transactionProvider;
  private BdsPendingTransactionService bdsPendingTransactionService;
  private TestScheduler scheduler;

  @Before public void setUp() throws Exception {
    when(transactionProvider.get(PACKAGE_NAME, SKU)).thenReturn(Single.just(
        new Transaction(UID, Transaction.Status.PROCESSING,
            new Gateway(Gateway.Name.appcoins, "", ""))), Single.just(
        new Transaction(UID, Transaction.Status.COMPLETED,
            new Gateway(Gateway.Name.appcoins, "", ""))));
    scheduler = new TestScheduler();
    bdsPendingTransactionService = new BdsPendingTransactionService(1, scheduler,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()),
        new CompositeDisposable(), transactionProvider);
  }

  @Test public void getTransaction() {
    bdsPendingTransactionService.start();
    scheduler.triggerActions();

    TestObserver<BdsPendingTransactionService.BdsTransaction> transactionObserver =
        new TestObserver<>();
    bdsPendingTransactionService.getTransaction(KEY)
        .subscribe(transactionObserver);

    TestObserver<Object> observer = new TestObserver<>();
    bdsPendingTransactionService.trackTransaction(KEY, PACKAGE_NAME, SKU)
        .subscribeOn(scheduler)
        .subscribe(observer);
    scheduler.triggerActions();
    scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

    observer.assertNoErrors()
        .assertComplete();

    BdsPendingTransactionService.BdsTransaction bdsTransaction =
        new BdsPendingTransactionService.BdsTransaction(KEY, PACKAGE_NAME, SKU);
    transactionObserver.assertNoErrors()
        .assertValues(new BdsPendingTransactionService.BdsTransaction(bdsTransaction,
                BdsPendingTransactionService.BdsTransaction.Status.PROCESSING),
            new BdsPendingTransactionService.BdsTransaction(bdsTransaction,
                BdsPendingTransactionService.BdsTransaction.Status.PROCESSING),
            new BdsPendingTransactionService.BdsTransaction(bdsTransaction,
                BdsPendingTransactionService.BdsTransaction.Status.COMPLETED));
  }
}