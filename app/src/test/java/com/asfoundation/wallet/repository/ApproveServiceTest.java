package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 3/16/18.
 */
public class ApproveServiceTest {
  public static final String PACKAGE_NAME = "package_name";
  @Mock SendTransactionInteract sendTransactionInteract;
  private ApproveService approveService;
  private PublishSubject<PendingTransaction> pendingTransactionState;
  private TestScheduler scheduler;

  @Before public void before() {
    MockitoAnnotations.initMocks(this);

    pendingTransactionState = PublishSubject.create();

    when(sendTransactionInteract.approve(any(TransactionBuilder.class),
        any(BigInteger.class))).thenReturn(Single.just("approve_hash"));

    when(sendTransactionInteract.buy(any(TransactionBuilder.class),
        any(BigInteger.class))).thenReturn(Single.just("buy_hash"));

    scheduler = new TestScheduler();
    approveService = new ApproveService(sendTransactionInteract,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler);
    approveService.start();
  }

  @Test public void approve() {
    String uri = "uri";
    TestObserver<PaymentTransaction> observer = new TestObserver<>();
    approveService.getApprove(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    approveService.approve(uri,
        new PaymentTransaction(uri, new TransactionBuilder("APPC"), PACKAGE_NAME))
        .subscribe();
    scheduler.triggerActions();

    pendingTransactionState.onNext(new PendingTransaction("hash", true));
    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", false));
    scheduler.triggerActions();

    List<PaymentTransaction> values = observer.values();
    Assert.assertEquals(values.size(), 3);
    Assert.assertEquals(values.get(2)
        .getState(), PaymentTransaction.PaymentState.APPROVED);
  }

  @Test public void approveTransactionNotFound() {
    when(sendTransactionInteract.buy(any(), any(BigInteger.class))).thenReturn(
        Single.just("buy_hash"));

    TestScheduler scheduler = new TestScheduler();
    ApproveService approveService = new ApproveService(sendTransactionInteract,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler);
    approveService.start();

    String uri = "uri";
    TestObserver<PaymentTransaction> observer = new TestObserver<>();
    approveService.getApprove(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    approveService.approve(uri,
        new PaymentTransaction(uri, new TransactionBuilder("APPC"), PACKAGE_NAME))
        .subscribe();
    scheduler.triggerActions();

    List<PaymentTransaction> values = observer.values();
    Assert.assertEquals(3, values.size());
    Assert.assertEquals(PaymentTransaction.PaymentState.APPROVED, values.get(2)
        .getState());
  }
}