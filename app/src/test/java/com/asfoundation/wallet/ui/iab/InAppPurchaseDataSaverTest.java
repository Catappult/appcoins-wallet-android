package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.poa.Proof;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.repository.InAppPurchaseService;
import com.asfoundation.wallet.repository.MemoryCache;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.ui.iab.database.InAppPurchaseData;
import io.reactivex.disposables.CompositeDisposable;
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
  public static final String APPLICATION_NAME = "application_name";
  public static final String PATH = "path";
  public static final String PRODUCT_NAME = "product_name";
  public static final String BUY_HASH_1 = "id1";
  @Mock InAppPurchaseService inAppPurchaseService;
  @Mock ProofOfAttentionService proofOfAttentionService;
  @Mock AppInfoProvider appInfoProvider;
  private BehaviorSubject<List<PaymentTransaction>> paymentSubject;
  private BehaviorSubject<List<Proof>> proofSubject;
  private AppcoinsOperationsDataSaver dataSaver;
  private TestScheduler scheduler;
  private MemoryCache<String, InAppPurchaseData> cache;

  @Before public void before()
      throws AppInfoProvider.UnknownApplicationException, ImageSaver.SaveException {
    paymentSubject = BehaviorSubject.create();
    when(inAppPurchaseService.getAll()).thenReturn(paymentSubject);
    when(appInfoProvider.get(BUY_HASH_1, PACKAGE_NAME, PRODUCT_NAME)).thenReturn(
        new InAppPurchaseData(BUY_HASH_1, PACKAGE_NAME, APPLICATION_NAME, PATH, PRODUCT_NAME));
    scheduler = new TestScheduler();
    cache = new MemoryCache<>(BehaviorSubject.create(), new HashMap<>());
    proofSubject = BehaviorSubject.create();
    when(proofOfAttentionService.get()).thenReturn(proofSubject);

    dataSaver =
        new AppcoinsOperationsDataSaver(inAppPurchaseService, proofOfAttentionService, cache,
            appInfoProvider, scheduler, new CompositeDisposable());
  }

  @Test public void start() {
    dataSaver.start();
    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.COMPLETED, APPROVE_HASH, BUY_HASH_1, BigInteger.ONE,
        PACKAGE_NAME, PRODUCT_NAME));
    paymentSubject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(
        new InAppPurchaseData(BUY_HASH_1, PACKAGE_NAME, APPLICATION_NAME, PATH, PRODUCT_NAME),
        cache.getSync(BUY_HASH_1));
  }

  @Test public void startWithoutAnyCompleted() {
    dataSaver.start();
    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.BUYING, APPROVE_HASH, BUY_HASH_1, BigInteger.ONE,
        PACKAGE_NAME, PRODUCT_NAME));
    paymentSubject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(null, cache.getSync(BUY_HASH_1));
  }

  @Test public void addAfterStop() {
    dataSaver.start();
    dataSaver.stop();

    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.COMPLETED, APPROVE_HASH, BUY_HASH_1, BigInteger.ONE,
        PACKAGE_NAME, PRODUCT_NAME));
    paymentSubject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(null, cache.getSync(BUY_HASH_1));
  }

  @Test public void removeDataAfterStop() {
    dataSaver.start();
    ArrayList<PaymentTransaction> list = new ArrayList<>();
    list.add(new PaymentTransaction(URI, new TransactionBuilder("APPC"),
        PaymentTransaction.PaymentState.COMPLETED, APPROVE_HASH, BUY_HASH_1, BigInteger.ONE,
        PACKAGE_NAME, PRODUCT_NAME));
    paymentSubject.onNext(list);
    scheduler.triggerActions();
    Assert.assertEquals(
        new InAppPurchaseData(BUY_HASH_1, PACKAGE_NAME, APPLICATION_NAME, PATH, PRODUCT_NAME),
        cache.getSync(BUY_HASH_1));

    dataSaver.stop();
    Assert.assertEquals(null, cache.getSync(BUY_HASH_1));
  }
}