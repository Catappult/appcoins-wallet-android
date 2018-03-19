package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 3/16/18.
 */
public class ApproveServiceTest {
  @Mock SendTransactionInteract sendTransactionInteract;
  @Mock PendingTransactionService pendingTransactionService;
  private ApproveService approveService;
  private PublishSubject<PendingTransaction> pendingTransactionState;

  @Before public void before() {
    MockitoAnnotations.initMocks(this);

    pendingTransactionState = PublishSubject.create();
    when(pendingTransactionService.checkTransactionState(anyString())).thenReturn(
        pendingTransactionState)
        .thenReturn(pendingTransactionState);

    when(sendTransactionInteract.approve(any(TransactionBuilder.class))).thenReturn(
        Single.just("approve_hash"));

    when(sendTransactionInteract.buy(any(TransactionBuilder.class))).thenReturn(
        Single.just("buy_hash"));

    approveService = new ApproveService(sendTransactionInteract, pendingTransactionService,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()));
    approveService.start();
  }

  @Test public void approve() throws Exception {
    String uri = "uri";
    TestObserver<PaymentTransaction> observer = new TestObserver<>();
    approveService.getApprove(uri)
        .subscribe(observer);

    approveService.approve(uri, new PaymentTransaction(uri, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.PENDING))
        .subscribe();

    pendingTransactionState.onNext(new PendingTransaction("hash", true));
    pendingTransactionState.onNext(new PendingTransaction("hash", false));

    List<PaymentTransaction> values = observer.values();
    Assert.assertEquals(values.size(), 4);
    Assert.assertEquals(values.get(3)
        .getState(), PaymentTransaction.PaymentState.APPROVED);
  }
}