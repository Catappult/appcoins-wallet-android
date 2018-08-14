package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.SendTransactionInteract;
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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 3/16/18.
 */
@RunWith(MockitoJUnitRunner.class) public class BuyServiceTest {

  public static final String PACKAGE_NAME = "package_name";
  public static final String PRODUCT_NAME = "product_name";
  @Mock SendTransactionInteract sendTransactionInteract;
  @Mock TrackTransactionService trackTransactionService;

  @Mock TransactionSender transactionSender;
  @Mock TransactionValidator transactionValidator;
  private TestScheduler scheduler;
  private WatchedTransactionService transactionService;
  private TransactionBuilder transactionBuilder;

  @Before public void setup() {
    transactionBuilder = new TransactionBuilder("APPC");
    when(transactionSender.send(transactionBuilder, BigInteger.ONE)).thenReturn(
        Single.just("hash"));

    scheduler = new TestScheduler();
    transactionService = new WatchedTransactionService(transactionSender,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler, trackTransactionService);
    when(transactionValidator.validate(any())).thenReturn(Completable.complete());
  }

  @Test public void buy() {
    PublishSubject<PendingTransaction> pendingTransactionState = PublishSubject.create();
    when(trackTransactionService.checkTransactionState(anyString())).thenReturn(
        pendingTransactionState);

    BuyService buyService = new BuyService(transactionService, transactionValidator);
    buyService.start();
    scheduler.triggerActions();

    String uri = "uri";
    TestObserver<BuyService.BuyTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);

    buyService.buy(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, BigInteger.ZERO, PACKAGE_NAME, PRODUCT_NAME))
        .subscribe();

    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", true));
    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", false));
    pendingTransactionState.onComplete();
    scheduler.triggerActions();
    List<BuyService.BuyTransaction> values = observer.values();
    Assert.assertEquals(values.size(), 3);
    Assert.assertEquals(values.get(2)
        .getStatus(), BuyService.Status.BOUGHT);
  }

  @Test public void buyTransactionNotFound() {
    Observable<PendingTransaction> pendingTransactionState =
        Observable.just(new PendingTransaction("hash", true),
            new PendingTransaction("hash", false));
    when(trackTransactionService.checkTransactionState("hash")).thenReturn(
        pendingTransactionState);

    BuyService buyService = new BuyService(transactionService, transactionValidator);
    buyService.start();

    String uri = "uri";
    TestObserver<BuyService.BuyTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    buyService.buy(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, BigInteger.ZERO, PACKAGE_NAME, PRODUCT_NAME))
        .subscribe();
    scheduler.triggerActions();

    List<BuyService.BuyTransaction> values = observer.values();
    Assert.assertEquals(3, values.size());
    Assert.assertEquals(BuyService.Status.BOUGHT, values.get(2)
        .getStatus());
  }
}