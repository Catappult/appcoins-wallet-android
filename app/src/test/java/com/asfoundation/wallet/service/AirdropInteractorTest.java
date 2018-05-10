package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.repository.EthereumNetworkRepository;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class AirdropInteractorTest {
  public static final String APPC_TRANSACTION_HASH = "appc_transaction";
  public static final String ETH_TRANSACTION_HASH = "eth_transaction";
  public static final String DESCRIPTION = "description";
  @Mock PendingTransactionService pendingTransactionService;
  @Mock PreferenceRepositoryType preference;
  @Mock TickerService tickerService;
  @Mock AirdropService.Api api;
  private BehaviorSubject<AirdropService.AirDropResponse> response;
  private AirdropService airdropService;
  private TestScheduler scheduler;
  private AirdropInteractor airdropInteractor;

  @Before public void setUp() {
    scheduler = new TestScheduler();
    response = BehaviorSubject.create();
    EthereumNetworkRepository ethereumNetworkRepository =
        new EthereumNetworkRepository(preference, tickerService);
    AirdropChainIdMapper airdropChainIdMapper = new AirdropChainIdMapper(
        new FindDefaultNetworkInteract(ethereumNetworkRepository, scheduler));
    airdropService = new AirdropService(api, new Gson(), scheduler);
    airdropInteractor = new AirdropInteractor(pendingTransactionService, ethereumNetworkRepository,
        BehaviorSubject.create(), airdropChainIdMapper, airdropService);

    when(pendingTransactionService.checkTransactionState(APPC_TRANSACTION_HASH)).thenReturn(
        Observable.just(new PendingTransaction(APPC_TRANSACTION_HASH, true)))
        .thenReturn(Observable.just(new PendingTransaction(APPC_TRANSACTION_HASH, false)));
    when(pendingTransactionService.checkTransactionState(ETH_TRANSACTION_HASH)).thenReturn(
        Observable.just(new PendingTransaction(ETH_TRANSACTION_HASH, true)))
        .thenReturn(Observable.just(new PendingTransaction(ETH_TRANSACTION_HASH, true)))
        .thenReturn(Observable.just(new PendingTransaction(ETH_TRANSACTION_HASH, false)));
  }

  @Test public void requestSuccess() {
    when(api.requestCoins(anyString(), anyInt())).thenReturn(response.firstOrError());
    TestObserver<Airdrop> test = airdropInteractor.getStatus()
        .test();
    airdropInteractor.request("wallet_address");
    response.onNext(
        new AirdropService.AirDropResponse(AirdropService.Status.OK, APPC_TRANSACTION_HASH,
            ETH_TRANSACTION_HASH, 1, DESCRIPTION));
    scheduler.triggerActions();
    test.assertValues(new Airdrop(Airdrop.AirdropStatus.PENDING, null),
        new Airdrop(Airdrop.AirdropStatus.SUCCESS, DESCRIPTION));
    test.assertNoErrors();
  }

  @Test public void requestFail() {
    when(api.requestCoins(anyString(), anyInt())).thenReturn(Single.error(new RuntimeException()));
    TestObserver<Airdrop> test = airdropInteractor.getStatus()
        .test();
    airdropInteractor.request("wallet_address");
    response.onNext(
        new AirdropService.AirDropResponse(AirdropService.Status.FAIL, APPC_TRANSACTION_HASH,
            ETH_TRANSACTION_HASH, 1, "description"));
    scheduler.triggerActions();
    test.assertValues(new Airdrop(Airdrop.AirdropStatus.PENDING, null),
        new Airdrop(Airdrop.AirdropStatus.ERROR, null));
  }

  @Test public void stop() {
    airdropInteractor.stop();
  }
}