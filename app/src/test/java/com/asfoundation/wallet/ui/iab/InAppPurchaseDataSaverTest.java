package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.MemoryCache;
import com.asfoundation.wallet.repository.PaymentTransaction;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class InAppPurchaseDataSaverTest {
  public static final String PACKAGE_NAME = "PACKAGE_NAME";
  public static final String URI = "uri";
  public static final String APPROVE_HASH = "approve_hash";
  public static final String BUY_HASH = "buy_hash";
  @Mock InAppPurchaseService inAppPurchaseService;
  private BehaviorSubject<List<PaymentTransaction>> subject;
  private InAppPurchaseDataSaver dataSaver;
  private TestScheduler scheduler;
  private MemoryCache<String, InAppPurchaseData> cache;

  @Before public void before() {
    subject = BehaviorSubject.create();
    when(inAppPurchaseService.getAll()).thenReturn(subject);
    scheduler = new TestScheduler();
    cache = new MemoryCache<>(BehaviorSubject.create(), new HashMap<>());
    dataSaver = new InAppPurchaseDataSaver(inAppPurchaseService, cache, scheduler);
  }

  @Test public void start() {
    dataSaver.start();
    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.COMPLETED, APPROVE_HASH, BUY_HASH, BigInteger.ONE,
        PACKAGE_NAME));
    subject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(new InAppPurchaseData(BUY_HASH), cache.getSync(BUY_HASH));
  }

  @Test public void startWithoutAnyCompleted() {
    dataSaver.start();
    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.BUYING, APPROVE_HASH, BUY_HASH, BigInteger.ONE,
        PACKAGE_NAME));
    subject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(null, cache.getSync(BUY_HASH));
  }

  @Test public void addAfterStop() {
    dataSaver.start();
    dataSaver.stop();

    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.COMPLETED, APPROVE_HASH, BUY_HASH, BigInteger.ONE,
        PACKAGE_NAME));
    subject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(null, cache.getSync(BUY_HASH));
  }

  @Test public void removeDataAfterStop() {
    dataSaver.start();
    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.COMPLETED, APPROVE_HASH, BUY_HASH, BigInteger.ONE,
        PACKAGE_NAME));
    subject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(new InAppPurchaseData(BUY_HASH), cache.getSync(BUY_HASH));

    dataSaver.stop();
    Assert.assertEquals(null, cache.getSync(BUY_HASH));
  }
}