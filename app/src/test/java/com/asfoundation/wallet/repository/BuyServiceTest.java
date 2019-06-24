package com.asfoundation.wallet.repository;

import com.appcoins.wallet.commons.MemoryCache;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import java.math.BigDecimal;
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
  public static final String PRODUCT_ID = "product_id";
  public static final String DEVELOPER_PAYLOAD = "developer_payload";
  public static final String STORE_ADDRESS = "0xc41b4160b63d1f9488937f7b66640d2babdbf8ad";
  public static final String OEM_ADDRESS = "0x0965b2a3e664690315ad20b9e5b0336c19cf172e";

  @Mock SendTransactionInteract sendTransactionInteract;
  @Mock TrackTransactionService trackTransactionService;

  @Mock TransactionSender transactionSender;
  @Mock TransactionValidator transactionValidator;
  @Mock DefaultTokenProvider defaultTokenProvider;
  @Mock CountryCodeProvider countryCodeProvider;
  private TestScheduler scheduler;
  private WatchedTransactionService transactionService;
  private TransactionBuilder transactionBuilder;
  private BuyService buyService;
  private Observable<PendingTransaction> pendingTransactionState;
  private String uri;
  private DataMapper dataMapper;
  @Mock AddressService addressService;

  @Before public void setup() {

    scheduler = new TestScheduler();
    transactionService = new WatchedTransactionService(transactionSender,
        new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>()), new ErrorMapper(),
        scheduler, trackTransactionService);
    when(transactionValidator.validate(any())).thenReturn(Completable.complete());
    TokenInfo tokenInfo =
        new TokenInfo("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "Appcoins", "APPC", 18, false,
            false);
    transactionBuilder =
        new TransactionBuilder("APPC", "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", 3l,
            "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", BigDecimal.ONE, "sku", 18,
            "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", "origin", "type", "packageName",
            "payload", null);
    when(transactionSender.send(transactionBuilder)).thenReturn(Single.just("hash"));
    when(defaultTokenProvider.getDefaultToken()).thenReturn(Single.just(tokenInfo));
    when(countryCodeProvider.getCountryCode()).thenReturn(Single.just("PT"));
    dataMapper = new DataMapper();

    pendingTransactionState = Observable.just(new PendingTransaction("hash", true),
        new PendingTransaction("hash", false));

    when(trackTransactionService.checkTransactionState(anyString())).thenReturn(
        pendingTransactionState);

    when(addressService.getStoreAddressForPackage(any())).thenReturn(Single.just(STORE_ADDRESS));
    when(addressService.getOemAddressForPackage(any())).thenReturn(Single.just(OEM_ADDRESS));

    buyService = new BuyService(transactionService, transactionValidator, defaultTokenProvider,
        countryCodeProvider, dataMapper, addressService);
    uri = "uri";
  }

  @Test public void buy() {
    buyService.start();
    TestObserver<BuyService.BuyTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);

    scheduler.triggerActions();

    buyService.buy(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, PACKAGE_NAME, PRODUCT_NAME, PRODUCT_ID, DEVELOPER_PAYLOAD, null, null))
        .subscribe();

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
    when(trackTransactionService.checkTransactionState("hash")).thenReturn(pendingTransactionState);

    BuyService buyService =
        new BuyService(transactionService, transactionValidator, defaultTokenProvider,
            countryCodeProvider, dataMapper, addressService);
    buyService.start();
    TestObserver<BuyService.BuyTransaction> observer = new TestObserver<>();
    buyService.getBuy(uri)
        .subscribe(observer);

    scheduler.triggerActions();

    buyService.buy(uri,
        new PaymentTransaction(uri, transactionBuilder, PaymentTransaction.PaymentState.APPROVED,
            "", null, PACKAGE_NAME, PRODUCT_NAME, PRODUCT_ID, DEVELOPER_PAYLOAD, null, null))
        .subscribe();

    scheduler.triggerActions();

    List<BuyService.BuyTransaction> values = observer.values();
    Assert.assertEquals(3, values.size());
    Assert.assertEquals(BuyService.Status.BOUGHT, values.get(2)
        .getStatus());
  }
}