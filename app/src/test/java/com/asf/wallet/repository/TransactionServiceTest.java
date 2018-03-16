package com.asf.wallet.repository;

import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.TokenInfo;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.interact.FetchGasSettingsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.util.TransferParser;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * Created by trinkes on 14/03/2018.
 */
public class TransactionServiceTest {

  public static final String CONTRACT_ADDRESS = "0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3";
  public static final String APPROVE_HASH = "approve_hash";
  public static final String BUY_HASH = "buy_hash";
  @Mock FetchGasSettingsInteract gasSettingsInteract;
  @Mock SendTransactionInteract sendTransactionInteract;
  @Mock PendingTransactionService pendingTransactionService;
  @Mock FindDefaultWalletInteract defaultWalletInteract;
  @Mock TokenRepositoryType tokenRepository;
  private TransactionService transactionService;
  private PublishSubject<PendingTransaction> pendingApproveState;
  private PublishSubject<PendingTransaction> pendingBuyState;

  @Before public void before() {
    MockitoAnnotations.initMocks(this);
    when(gasSettingsInteract.fetch(anyBoolean())).thenReturn(
        Single.just(new GasSettings(new BigDecimal(1), new BigDecimal(2))));

    when(sendTransactionInteract.approve(any(TransactionBuilder.class))).thenReturn(
        Single.just(APPROVE_HASH));

    when(sendTransactionInteract.buy(any(TransactionBuilder.class))).thenReturn(
        Single.just(BUY_HASH));
    pendingApproveState = PublishSubject.create();
    pendingBuyState = PublishSubject.create();
    when(pendingTransactionService.checkTransactionState(APPROVE_HASH)).thenReturn(
        pendingApproveState);
    when(pendingTransactionService.checkTransactionState(BUY_HASH)).thenReturn(pendingBuyState);

    when(defaultWalletInteract.find()).thenReturn(Single.just(new Wallet("wallet_address")));

    Token token = new Token(new TokenInfo(CONTRACT_ADDRESS, "AppCoins", "APPC", 18, true, true),
        new BigDecimal(10), 32L);
    Token[] tokens = new Token[1];
    tokens[0] = token;
    when(tokenRepository.fetchAll(any())).thenReturn(Observable.just(tokens));

    transactionService = new TransactionService(gasSettingsInteract, sendTransactionInteract,
        pendingTransactionService, defaultWalletInteract,
        new TransferParser(defaultWalletInteract, tokenRepository),
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        new ApproveService(sendTransactionInteract, pendingTransactionService,
            new MemoryCache<>(BehaviorSubject.create(), new HashMap<>())),
        new BuyService(sendTransactionInteract, pendingTransactionService,
            new MemoryCache<>(BehaviorSubject.create(), new HashMap<>())));
  }

  @Test public void sendTransaction() throws Exception {
    TestObserver<PendingTransaction> testObserver = new TestObserver<>();
    transactionService.sendTransaction("ethereum:"
        + CONTRACT_ADDRESS
        + "@3"
        + "/transfer?uint256=1000000000000000000&address"
        + "=0x4fbcc5ce88493c3d9903701c143af65f54481119&data=0x636f6d2e63656e61732e70726f64756374")
        .subscribe(testObserver);

    PendingTransaction pendingTransaction0 = new PendingTransaction("approve_hash", true);
    PendingTransaction pendingTransaction1 = new PendingTransaction("approve_hash", false);
    PendingTransaction pendingTransaction2 = new PendingTransaction("buy_hash", true);
    PendingTransaction pendingTransaction3 = new PendingTransaction("buy_hash", false);

    pendingApproveState.onNext(pendingTransaction0);
    pendingApproveState.onNext(pendingTransaction1);
    pendingBuyState.onNext(pendingTransaction2);
    pendingBuyState.onNext(pendingTransaction3);

    List<PendingTransaction> values = testObserver.assertNoErrors()
        .values();
    Assert.assertEquals(values.get(0), pendingTransaction0);
    Assert.assertEquals(values.get(1), pendingTransaction2);
    Assert.assertEquals(values.get(2), pendingTransaction3);
    Assert.assertEquals(values.size(), 3);
  }

  @Test public void sendTransaction2() throws Exception {
    String uri = "ethereum:"
        + CONTRACT_ADDRESS
        + "@3"
        + "/transfer?uint256=1000000000000000000&address"
        + "=0x4fbcc5ce88493c3d9903701c143af65f54481119&data=0x636f6d2e63656e61732e70726f64756374";
    transactionService.start();
    TestObserver<PaymentTransaction> testObserver = new TestObserver<>();
    transactionService.getTransactionState(uri)
        .doOnNext(paymentTransaction -> System.out.println(paymentTransaction))
        .subscribe(testObserver);
    transactionService.send(uri)
        .subscribe();

    PendingTransaction pendingTransaction0 = new PendingTransaction("approve_hash", true);
    PendingTransaction pendingTransaction1 = new PendingTransaction("approve_hash", false);
    PendingTransaction pendingTransaction2 = new PendingTransaction("buy_hash", true);
    PendingTransaction pendingTransaction3 = new PendingTransaction("buy_hash", false);

    pendingApproveState.onNext(pendingTransaction0);
    pendingApproveState.onNext(pendingTransaction1);
    pendingBuyState.onNext(pendingTransaction2);
    pendingBuyState.onNext(pendingTransaction3);

    List<PaymentTransaction> values = testObserver.assertNoErrors()
        .values();
  }
}