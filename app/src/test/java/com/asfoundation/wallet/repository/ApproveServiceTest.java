package com.asfoundation.wallet.repository;

import com.appcoins.wallet.commons.MemoryCache;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Observable;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 3/16/18.
 */
public class ApproveServiceTest {
  public static final String PACKAGE_NAME = "package_name";
  public static final String PRODUCT_NAME = "product_name";
  public static final String APPROVE_HASH = "approve_hash";
  public static final String DEVELOPER_PAYLOAD = "developer_payload";
  @Mock TrackTransactionService trackTransactionService;
  @Mock TransactionSender transactionSender;
  @Mock TransactionValidator transactionValidator;
  private ApproveService approveService;
  private PublishSubject<PendingTransaction> pendingTransactionState;
  private TestScheduler scheduler;
  private WatchedTransactionService transactionService;
  private TransactionBuilder transactionBuilder;
  private BigInteger nonce;

  @Before public void before() {
    MockitoAnnotations.initMocks(this);
    transactionBuilder = new TransactionBuilder("APPC");

    pendingTransactionState = PublishSubject.create();
    nonce = BigInteger.ZERO;

    when(transactionSender.send(transactionBuilder)).thenReturn(Single.just(APPROVE_HASH));

    scheduler = new TestScheduler();
    transactionService = new WatchedTransactionService(transactionSender,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler, trackTransactionService);

    when(transactionValidator.validate(any())).thenReturn(Completable.complete());
    approveService = new ApproveService(transactionService, transactionValidator);
    approveService.start();
  }

  @Test public void approve() {
    String uri = "uri";
    TestObserver<ApproveService.ApproveTransaction> observer = new TestObserver<>();
    when(trackTransactionService.checkTransactionState(APPROVE_HASH)).thenReturn(
        Observable.just(new PendingTransaction(APPROVE_HASH, false)));
    approveService.getApprove(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    approveService.approve(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, PACKAGE_NAME, PRODUCT_NAME, DEVELOPER_PAYLOAD, null, null))
        .subscribe();
    scheduler.triggerActions();

    pendingTransactionState.onNext(new PendingTransaction(APPROVE_HASH, true));
    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction(APPROVE_HASH, false));
    scheduler.triggerActions();

    List<ApproveService.ApproveTransaction> values = observer.values();
    Assert.assertEquals(values.size(), 3);
    Assert.assertEquals(values.get(2)
        .getStatus(), ApproveService.Status.APPROVED);
  }

  @Test public void approveTransactionNotFound() {
    String uri = "uri";
    doAnswer(new Answer() {
      private int count = 0;

      public Observable<PendingTransaction> answer(InvocationOnMock invocation) {
        if (count++ == 1) return Observable.error(new TransactionNotFoundException());
        return Observable.just(new PendingTransaction(APPROVE_HASH, false));
      }
    }).when(trackTransactionService)
        .checkTransactionState(APPROVE_HASH);
    TestObserver<ApproveService.ApproveTransaction> observer = new TestObserver<>();
    approveService.getApprove(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    approveService.approve(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, PACKAGE_NAME, PRODUCT_NAME, DEVELOPER_PAYLOAD, null, null))
        .subscribe();
    scheduler.triggerActions();

    List<ApproveService.ApproveTransaction> values = observer.values();
    Assert.assertEquals(3, values.size());
    Assert.assertEquals(ApproveService.Status.APPROVED, values.get(2)
        .getStatus());
  }
}