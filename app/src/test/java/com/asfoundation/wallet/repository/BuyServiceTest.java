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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 3/16/18.
 */
@RunWith(MockitoJUnitRunner.class) public class BuyServiceTest {

  @Mock SendTransactionInteract sendTransactionInteract;

  @Test public void buy() {
    PublishSubject<PendingTransaction> pendingTransactionState = PublishSubject.create();
    when(sendTransactionInteract.buy(any(), any(BigInteger.class))).thenReturn(
        Single.just("buy_hash"));

    TestScheduler scheduler = new TestScheduler();
    BuyService buyService = new BuyService(sendTransactionInteract,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler);
    buyService.start();

    String uri = "uri";
    TestObserver<PaymentTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);

    buyService.buy(uri, new PaymentTransaction(uri, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.PENDING))
        .subscribe();

    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", true));
    scheduler.triggerActions();
    pendingTransactionState.onNext(new PendingTransaction("hash", false));
    scheduler.triggerActions();
    List<PaymentTransaction> values = observer.values();
    Assert.assertEquals(values.size(), 3);
    Assert.assertEquals(values.get(2)
        .getState(), PaymentTransaction.PaymentState.BOUGHT);
  }

  @Test public void buyTransactionNotFound() {
    when(sendTransactionInteract.buy(any(), any(BigInteger.class))).thenReturn(
        Single.just("buy_hash"));

    TestScheduler scheduler = new TestScheduler();
    BuyService buyService = new BuyService(sendTransactionInteract,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler);
    buyService.start();

    String uri = "uri";
    TestObserver<PaymentTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);
    scheduler.triggerActions();
    buyService.buy(uri, new PaymentTransaction(uri, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.PENDING))
        .subscribe();
    scheduler.triggerActions();

    List<PaymentTransaction> values = observer.values();
    Assert.assertEquals(3, values.size());
    Assert.assertEquals(PaymentTransaction.PaymentState.BOUGHT, values.get(2)
        .getState());
  }
}