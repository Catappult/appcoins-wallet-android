package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.math.BigDecimal;
import java.math.BigInteger;
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
  @Mock NonceGetter nonceGetter;
  @Mock BalanceService balanceService;
  private InAppPurchaseService transactionService;
  private PublishSubject<PendingTransaction> pendingApproveState;
  private PublishSubject<PendingTransaction> pendingBuyState;
  private PublishSubject<GetDefaultWalletBalance.BalanceState> balance;
  private TestScheduler scheduler;

  @Before public void before() {
    MockitoAnnotations.initMocks(this);
    balance = PublishSubject.create();
    when(gasSettingsInteract.fetch(anyBoolean())).thenReturn(
        Single.just(new GasSettings(new BigDecimal(1), new BigDecimal(2))));

    when(sendTransactionInteract.approve(any(TransactionBuilder.class),
        any(BigInteger.class))).thenReturn(Single.just(APPROVE_HASH));

    when(sendTransactionInteract.buy(any(TransactionBuilder.class),
        any(BigInteger.class))).thenReturn(Single.just(BUY_HASH));

    when(nonceGetter.getNonce()).thenReturn(Single.just(BigInteger.ONE));
    pendingApproveState = PublishSubject.create();
    pendingBuyState = PublishSubject.create();
    when(pendingTransactionService.checkTransactionState(APPROVE_HASH)).thenReturn(
        pendingApproveState);
    when(pendingTransactionService.checkTransactionState(BUY_HASH)).thenReturn(pendingBuyState);
    when(balanceService.hasEnoughBalance(any(TransactionBuilder.class),
        any(BigDecimal.class))).thenReturn(balance.firstOrError());

    when(defaultWalletInteract.find()).thenReturn(Single.just(new Wallet("wallet_address")));

    Token token = new Token(new TokenInfo(CONTRACT_ADDRESS, "AppCoins", "APPC", 18, true, true),
        new BigDecimal(10), 32L);
    Token[] tokens = new Token[1];
    tokens[0] = token;
    when(tokenRepository.fetchAll(any())).thenReturn(Observable.just(tokens));

    scheduler = new TestScheduler();
    transactionService = new InAppPurchaseService(gasSettingsInteract, defaultWalletInteract,
        new TransferParser(defaultWalletInteract, tokenRepository),
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()),
        new ApproveService(sendTransactionInteract,
            new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), new ErrorMapper(),
            scheduler), new BuyService(sendTransactionInteract, pendingTransactionService,
        new MemoryCache<>(BehaviorSubject.create(), new HashMap<>()), new ErrorMapper(), scheduler),
        nonceGetter, balanceService, BigDecimal.ONE);
  }

  @Test public void sendTransaction() {
    String uri = "ethereum:"
        + CONTRACT_ADDRESS
        + "@3"
        + "/transfer?uint256=1000000000000000000&address"
        + "=0x4fbcc5ce88493c3d9903701c143af65f54481119&data=0x636f6d2e63656e61732e70726f64756374";
    transactionService.start();
    TestObserver<PaymentTransaction> testObserver = new TestObserver<>();
    transactionService.getTransactionState(uri)
        .subscribe(testObserver);
    scheduler.triggerActions();
    transactionService.send(uri)
        .subscribe();
    scheduler.triggerActions();
    balance.onNext(GetDefaultWalletBalance.BalanceState.OK);

    PendingTransaction pendingTransaction0 = new PendingTransaction("approve_hash", true);
    PendingTransaction pendingTransaction1 = new PendingTransaction("approve_hash", false);
    PendingTransaction pendingTransaction2 = new PendingTransaction("buy_hash", true);
    PendingTransaction pendingTransaction3 = new PendingTransaction("buy_hash", false);

    pendingApproveState.onNext(pendingTransaction0);
    scheduler.triggerActions();
    pendingApproveState.onNext(pendingTransaction1);
    scheduler.triggerActions();
    pendingBuyState.onNext(pendingTransaction2);
    scheduler.triggerActions();
    pendingBuyState.onNext(pendingTransaction3);
    scheduler.triggerActions();

    List<PaymentTransaction> values = testObserver.assertNoErrors()
        .values();
    int index = 0;
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.PENDING));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.PENDING));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.APPROVING));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.APPROVED));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.BUYING));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.BOUGHT));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.COMPLETED));
    Assert.assertTrue(values.size() == 7);
  }

  @Test public void sendTransactionNoEtherFunds() {
    String uri = "ethereum:"
        + CONTRACT_ADDRESS
        + "@3"
        + "/transfer?uint256=1000000000000000000&address"
        + "=0x4fbcc5ce88493c3d9903701c143af65f54481119&data=0x636f6d2e63656e61732e70726f64756374";
    transactionService.start();
    TestObserver<PaymentTransaction> testObserver = new TestObserver<>();
    transactionService.getTransactionState(uri)
        .subscribe(testObserver);
    scheduler.triggerActions();
    transactionService.send(uri)
        .subscribe();
    scheduler.triggerActions();
    balance.onNext(GetDefaultWalletBalance.BalanceState.NO_ETHER);

    PendingTransaction pendingTransaction0 = new PendingTransaction("approve_hash", true);
    PendingTransaction pendingTransaction1 = new PendingTransaction("approve_hash", false);
    PendingTransaction pendingTransaction2 = new PendingTransaction("buy_hash", true);
    PendingTransaction pendingTransaction3 = new PendingTransaction("buy_hash", false);

    pendingApproveState.onNext(pendingTransaction0);
    scheduler.triggerActions();
    pendingApproveState.onNext(pendingTransaction1);
    scheduler.triggerActions();
    pendingBuyState.onNext(pendingTransaction2);
    scheduler.triggerActions();
    pendingBuyState.onNext(pendingTransaction3);
    scheduler.triggerActions();

    List<PaymentTransaction> values = testObserver.assertNoErrors()
        .values();
    int index = 0;
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.PENDING));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.NO_ETHER));
    Assert.assertTrue(values.size() == 2);
  }

  @Test public void sendTransactionNoFunds() {
    String uri = "ethereum:"
        + CONTRACT_ADDRESS
        + "@3"
        + "/transfer?uint256=1000000000000000000&address"
        + "=0x4fbcc5ce88493c3d9903701c143af65f54481119&data=0x636f6d2e63656e61732e70726f64756374";
    transactionService.start();
    TestObserver<PaymentTransaction> testObserver = new TestObserver<>();
    transactionService.getTransactionState(uri)
        .subscribe(testObserver);
    scheduler.triggerActions();
    transactionService.send(uri)
        .subscribe();
    scheduler.triggerActions();
    balance.onNext(GetDefaultWalletBalance.BalanceState.NO_ETHER_NO_TOKEN);

    PendingTransaction pendingTransaction0 = new PendingTransaction("approve_hash", true);
    PendingTransaction pendingTransaction1 = new PendingTransaction("approve_hash", false);
    PendingTransaction pendingTransaction2 = new PendingTransaction("buy_hash", true);
    PendingTransaction pendingTransaction3 = new PendingTransaction("buy_hash", false);

    pendingApproveState.onNext(pendingTransaction0);
    scheduler.triggerActions();
    pendingApproveState.onNext(pendingTransaction1);
    scheduler.triggerActions();
    balance.onNext(GetDefaultWalletBalance.BalanceState.NO_ETHER_NO_TOKEN);
    pendingBuyState.onNext(pendingTransaction2);
    scheduler.triggerActions();
    balance.onNext(GetDefaultWalletBalance.BalanceState.NO_ETHER_NO_TOKEN);
    pendingBuyState.onNext(pendingTransaction3);
    scheduler.triggerActions();
    balance.onNext(GetDefaultWalletBalance.BalanceState.NO_ETHER_NO_TOKEN);

    List<PaymentTransaction> values = testObserver.assertNoErrors()
        .values();
    int index = 0;
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.PENDING));
    Assert.assertTrue(values.get(index++)
        .getState()
        .equals(PaymentTransaction.PaymentState.NO_FUNDS));
    Assert.assertTrue(values.size() == 2);
  }
}